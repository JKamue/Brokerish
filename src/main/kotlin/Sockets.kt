package de.jkamue

import de.jkamue.header.controlpacket.ControlPacketType
import de.jkamue.packets.connect.parsing.ConnectPacketParser
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.io.EOFException


object TlsRawSocket {

    @JvmStatic
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
                            println("Received MQTT packet (${packet.size} bytes): " +
                                    packet.joinToString(" ") { "%02x".format(it) })
                        }
                        println("Connection closed by peer: ${socket.remoteAddress}")
                    } catch (e: Throwable) {
                        println("Client handler error (${socket.remoteAddress}): ${e.message}")
                    } finally {
                        try { socket.close() } catch (_: Throwable) {}
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
    suspend fun readMqttPacket(channel: ByteReadChannel): ByteArray? {
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
            val full = ByteArray(1 + remainingLengthBytes.size + payload.size)
            full[0] = firstByte
            for (i in remainingLengthBytes.indices) full[1 + i] = remainingLengthBytes[i]
            if (payload.isNotEmpty()) System.arraycopy(payload, 0, full, 1 + remainingLengthBytes.size, payload.size)

            val test = full.joinToString(" ") { "%02x".format(it) }

            ConnectPacketParser(payload).parseConnectPacket()

            return full
        } catch (ex: Throwable) {
            // Re-throw or return null depending on desired behavior.
            // For now, propagate to caller for logging.
            throw ex
        }
    }

    fun validateFirstByte() {

    }


}
