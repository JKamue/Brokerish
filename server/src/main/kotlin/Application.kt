package de.jkamue

import de.jkamue.mqtt.MalformedPacketMqttException
import de.jkamue.mqtt.packet.ConnectPacket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mqtt.parser.ControlPacketType
import mqtt.parser.connect.ConnectPacketParser
import java.io.EOFException
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
suspend fun readMqttPacket(channel: ByteReadChannel): ConnectPacket? {
    try {
        // Read first byte (control packet type + flags)
        val firstByte = channel.readByte()
        val type = ControlPacketType.detect(firstByte.toInt())

        // Variable Length decoding as described in 1.5.5
        var multiplier = 1
        var value = 0
        val remainingLengthBytes = mutableListOf<Byte>()
        do {
            val nextByte = channel.readByte()
            val encoded = nextByte.toInt() and 0b11111111
            remainingLengthBytes.add(nextByte)
            value += (encoded and 0b01111111) * multiplier
            multiplier *= 128
            if (multiplier > 128 * 128 * 128) throw MalformedPacketMqttException("Malformed Variable Byte Integer")
        } while ((remainingLengthBytes.last().toInt() and 0b10000000) != 0)

        // Now read exactly `value` bytes for variable header + payload
        val payload = ByteArray(value)
        if (value > 0) {
            try {
                channel.readFully(payload, 0, value) // suspends until all requested bytes are available or EOF
            } catch (e: EOFException) {
                // stream closed unexpectedly
                return null
            }
        }

        // Combine into one full packet array for convenience
        var packet: ConnectPacket? = null
        val parsingTimeNanos = measureNanoTime {
            packet = ConnectPacketParser.parseConnectPacket(payload)
        }
        val parsingTimeMicros = parsingTimeNanos / 1000.0
        println("Parsing took $parsingTimeNanos ns (or $parsingTimeMicros µs).")
        return packet
    } catch (ex: Throwable) {
        throw ex
    }
}
