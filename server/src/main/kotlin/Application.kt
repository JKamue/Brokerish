package de.jkamue

import de.jkamue.mqtt.ConnectReasonCode
import de.jkamue.mqtt.MalformedPacketMqttException
import de.jkamue.mqtt.packet.ConnackPacket
import de.jkamue.mqtt.packet.ControlPacketType
import de.jkamue.mqtt.packet.Packet
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mqtt.encoder.ConnackEncoder
import mqtt.parser.PacketParser
import java.nio.ByteBuffer
import kotlin.system.measureNanoTime

fun main(args: Array<String>) {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", 9002)
        println("MQTT-listening server is running at ${serverSocket.localAddress}")

        while (true) {
            val socket = serverSocket.accept()
            println("Accepted connection from ${socket.remoteAddress}")

            // Handle each client in its own coroutine
            launch {
                val receive = socket.openReadChannel()
                try {
                    while (true) {
                        val packet = readMqttPacket(receive) ?: break
                        println("Received MQTT packet $packet")
                        val connackPacket = ConnackPacket(false, ConnectReasonCode.SUCCESS)
                        val writeChannel =
                            socket.openWriteChannel(autoFlush = true) // or false if you want manual flush
                        val connackBuffers = ConnackEncoder.encodeScatter(connackPacket)
                        sendScatter(writeChannel, connackBuffers)
                    }
                    println("Connection closed by peer: ${socket.remoteAddress}")
                } catch (e: Throwable) {
                    println("Client handler error (${socket.remoteAddress}): ${e.message}")
                } finally {
                    try {
                        socket.close()
                    } catch (ex: Throwable) {
                        println(ex)
                    }
                }
            }
        }
    }
}

/**
 * Reads a single MQTT Control Packet from the provided ByteReadChannel.
 * Returns the entire packet as a ByteArray:
 * [firstByte][encodedRemainingLengthBytes][payload]
 *
 * Returns null when the channel is closed / EOF reached.
 */
suspend fun readMqttPacket(channel: ByteReadChannel): Packet? {
    try {
        val controlPacketType = readControlPacketType(channel)
        val content = getPacketContent(channel)

        var packet: Packet? = null
        val parsingTimeNanos = measureNanoTime {
            packet = PacketParser.parsePacket(content, controlPacketType)
        }
        val parsingTimeMicros = parsingTimeNanos / 1000.0
        println("Parsing took $parsingTimeNanos ns (or $parsingTimeMicros µs).")
        return packet
    } catch (ex: Throwable) {
        throw ex
    }
}

suspend fun readControlPacketType(channel: ByteReadChannel): ControlPacketType {
    val firstByte = channel.readByte()
    return ControlPacketType.detect(firstByte.toInt() and 0xFF)
}

suspend fun getPacketContent(channel: ByteReadChannel): ByteArray {
    val contentLength = getPacketContentLength(channel)
    val payload = ByteArray(contentLength)
    channel.readFully(payload, 0, contentLength)
    return payload
}

suspend fun getPacketContentLength(channel: ByteReadChannel): Int {
    // Variable Length decoding as described in 1.5.5
    var multiplier = 1
    var contentLength = 0
    val remainingLengthBytes = mutableListOf<Byte>()
    do {
        val nextByte = channel.readByte()
        val encoded = nextByte.toInt() and 0b11111111
        remainingLengthBytes.add(nextByte)
        contentLength += (encoded and 0b01111111) * multiplier
        multiplier *= 128
        if (multiplier > 128 * 128 * 128) throw MalformedPacketMqttException("Malformed Variable Byte Integer")
    } while ((remainingLengthBytes.last().toInt() and 0b10000000) != 0)
    return contentLength
}


suspend fun sendScatter(socketWrite: ByteWriteChannel, parts: Array<ByteBuffer>) {
    for (part in parts) {
        part.rewind()
        socketWrite.writeFully(part)
    }
}
