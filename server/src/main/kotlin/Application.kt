package de.jkamue

import de.jkamue.Settings.SERVER_MAX_PACKET_SIZE
import de.jkamue.mqtt.MalformedPacketMqttException
import de.jkamue.mqtt.logic.ClientConnected
import de.jkamue.mqtt.logic.ClientDisconnected
import de.jkamue.mqtt.logic.MqttServer
import de.jkamue.mqtt.logic.PacketReceived
import de.jkamue.mqtt.packet.ConnectPacket
import de.jkamue.mqtt.packet.ControlPacketType
import de.jkamue.mqtt.packet.DisconnectPacket
import de.jkamue.mqtt.packet.Packet
import de.jkamue.mqtt.valueobject.ClientId
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mqtt.encoder.PacketEncoder
import mqtt.parser.PacketParser
import java.nio.ByteBuffer
import kotlin.system.measureNanoTime

fun main() {
    runBlocking {
        val selectorManager = ActorSelectorManager(Dispatchers.IO)
        val serverJob = SupervisorJob()
        val serverScope = CoroutineScope(Dispatchers.IO + serverJob)

        val mqttServer = MqttServer(serverScope)

        aSocket(selectorManager).tcp().bind("127.0.0.1", 9002).use { serverSocket ->
            log("MQTT-listening server is running at ${serverSocket.localAddress}")

            while (true) {
                val socket = serverSocket.accept()

                serverScope.launch {
                    log("Accepted connection from ${socket.remoteAddress}")
                    val readChannel = socket.openReadChannel()
                    val readBuffer = ByteArray(SERVER_MAX_PACKET_SIZE)
                    val writeChannel = socket.openWriteChannel(autoFlush = false)
                    val outgoingPackets = Channel<Packet>(Channel.BUFFERED)
                    var clientId: ClientId? = null // Hold clientId for the finally block

                    try {
                        val firstPacket = readMqttPacket(readChannel, readBuffer)
                        if (firstPacket !is ConnectPacket) {
                            log("First packet was not CONNECT, closing connection.")
                            socket.close()
                            return@launch
                        }
                        clientId = firstPacket.clientId

                        mqttServer.commandChannel.send(ClientConnected(clientId, outgoingPackets))
                        mqttServer.commandChannel.send(PacketReceived(clientId, firstPacket))

                        // Writer coroutine
                        launch {
                            for (packet in outgoingPackets) {
                                log("Sending packet ${packet.packetType} to $clientId.")
                                val encoded = PacketEncoder.encodeScatter(packet)
                                sendScatter(writeChannel, encoded)
                                if (packet is DisconnectPacket) {
                                    socket.close() // Close the socket after sending DISCONNECT
                                }
                            }
                        }

                        // Reader coroutine
                        while (socket.isActive) {
                            val packet = readMqttPacket(readChannel, readBuffer) ?: break // Connection closed
                            mqttServer.commandChannel.send(PacketReceived(clientId, packet))
                        }

                    } catch (e: Exception) {
                        log("Error in client coroutine for ${socket.remoteAddress}: ${e.message}")
                    } finally {
                        log("Closing connection for ${socket.remoteAddress}")
                        clientId?.let { mqttServer.commandChannel.send(ClientDisconnected(it)) }
                        outgoingPackets.close()
                        socket.close()
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
suspend fun readMqttPacket(channel: ByteReadChannel, reusableBuffer: ByteArray): Packet? {
    try {
        val controlPacketType = try {
            readControlPacketType(channel)
        } catch (e: java.io.EOFException) {
            log("readMqttPacket: EOF while reading first byte -> connection closed by peer")
            return null
        }
        log("Starting to receive packet of type $controlPacketType")

        val content = try {
            getPacketContent(channel, reusableBuffer)
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

suspend fun getPacketContent(channel: ByteReadChannel, reusableBuffer: ByteArray): ByteBuffer {
    val packetBodyLength = getPacketContentLength(channel)
    if (packetBodyLength < 0) throw MalformedPacketMqttException("Negative content length")
    if (packetBodyLength > SERVER_MAX_PACKET_SIZE) throw MalformedPacketMqttException("Package too large")

    channel.readFully(reusableBuffer, 0, packetBodyLength)
    val wrapper = ByteBuffer.wrap(reusableBuffer)
    wrapper.limit(packetBodyLength)
    return wrapper.slice().asReadOnlyBuffer()
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