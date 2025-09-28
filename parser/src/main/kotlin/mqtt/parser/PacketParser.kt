package mqtt.parser

import de.jkamue.mqtt.packet.ControlPacketType
import de.jkamue.mqtt.packet.Packet
import mqtt.parser.connect.ConnectPacketParser

object PacketParser {
    fun parsePacket(bytes: ByteArray, packetType: ControlPacketType): Packet {
        val buffer = MQTTByteBuffer.wrap(bytes)
        return when (packetType) {
            ControlPacketType.CONNECT -> ConnectPacketParser.parseConnectPacket(buffer)
            else -> throw NotImplementedError("Control Packet not implemented yet")
        }
    }
}