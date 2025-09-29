package de.jkamue

import de.jkamue.mqtt.MalformedPacketMqttException
import de.jkamue.mqtt.logic.MqttServer
import de.jkamue.mqtt.packet.ConnectPacket
import de.jkamue.mqtt.packet.ControlPacketType
import de.jkamue.mqtt.packet.Packet
import de.jkamue.mqtt.valueobject.ClientId
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mqtt.encoder.PacketEncoder
import mqtt.parser.PacketParser
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureNanoTime

val sessions = ConcurrentHashMap<ClientId, ClientSession>()

data class ClientSession(
    val writer: ByteWriteChannel,
    val outgoing: Channel<Array<ByteBuffer>>
)

fun main(args: Array<String>) {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", 9002)
        log("MQTT-listening server is running at ${serverSocket.localAddress}")

        while (true) {
            val socket = serverSocket.accept()
            log("Accepted connection from ${socket.remoteAddress}")

            // Handle each client in its own coroutine
            launch {
                val readChannel = socket.openReadChannel()
                val writeChannel = socket.openWriteChannel()

                val session = ClientSession(writeChannel, Channel(Channel.UNLIMITED))

                val connectPacket =
                    readMqttPacket(readChannel)
                        ?: throw MalformedPacketMqttException("First CONNECT packet not readable")
                if (connectPacket.packetType != ControlPacketType.CONNECT) throw MalformedPacketMqttException("First packet is no connect packet")
                val response = MqttServer.processConnectPacket(connectPacket as ConnectPacket)
                val clientId = response.first
                sessions[clientId] = session
                val encodedResponse = PacketEncoder.encodeScatter(response.second)
                sendScatter(writeChannel, encodedResponse)

                launch {
                    for (message in session.outgoing) {
                        sendScatter(writeChannel, message)
                    }
                }

                try {
                    while (true) {
                        val packet = readMqttPacket(readChannel) ?: break
                        log("Received MQTT packet $packet")
                        val response = MqttServer.processPacket(clientId, packet)

                        for (packetToSend in response) {
                            log("Sending ${packetToSend.first} ${packetToSend.second}")
                            // TODO: Encode on sending side to not block socket here potentially?
                            val encodedPacket = PacketEncoder.encodeScatter(packetToSend.second)
                            sessions[packetToSend.first]?.outgoing?.send(encodedPacket)
                        }
                    }
                    log("Connection closed by peer: ${socket.remoteAddress}")
                } catch (e: Throwable) {
                    log("Client handler error (${socket.remoteAddress}): ${e.message}")
                    e.printStackTrace()
                } finally {
                    try {
                        sessions.remove(clientId)
                        session.outgoing.close()
                        socket.close()
                    } catch (ex: Throwable) {
                        log(ex.toString())
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
        val controlPacketType = try {
            readControlPacketType(channel)
        } catch (e: java.io.EOFException) {
            log("readMqttPacket: EOF while reading first byte -> connection closed by peer")
            return null
        }
        log("Starting to receive packet of type $controlPacketType")

        val content = try {
            getPacketContent(channel)
        } catch (e: java.io.EOFException) {
            log("readMqttPacket: EOF while reading remaining length / payload -> connection closed by peer")
            return null
        }

        var packet: Packet? = null
        val parsingTimeNanos = measureNanoTime {
            packet = PacketParser.parsePacket(content, controlPacketType)
        }
        val parsingTimeMicros = parsingTimeNanos / 1000.0
        log("Parsing took $parsingTimeNanos ns (or $parsingTimeMicros µs).")
        return packet
    } catch (ex: MalformedPacketMqttException) {
        // rethrow known MQTT protocol errors to be handled by caller if desired
        throw ex
    } catch (ex: Throwable) {
        // unexpected errors: log and rethrow so outer handler can close the socket and print stack trace
        log("readMqttPacket: unexpected error: ${ex.message}")
        throw ex
    }
}

suspend fun readControlPacketType(channel: ByteReadChannel): ControlPacketType {
    val firstByte = channel.readByte()
    return ControlPacketType.detect(firstByte.toInt() and 0xFF)
}

suspend fun getPacketContent(channel: ByteReadChannel): ByteArray {
    val contentLength = getPacketContentLength(channel)
    if (contentLength < 0) throw MalformedPacketMqttException("Negative content length")
    val payload = ByteArray(contentLength)
    if (contentLength > 0) {
        channel.readFully(payload, 0, contentLength)
    }
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

fun log(msg: String) {
    AsyncLogger.log(msg)
}