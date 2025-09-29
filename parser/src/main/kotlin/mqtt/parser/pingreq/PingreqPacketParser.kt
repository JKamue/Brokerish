package mqtt.parser.pingreq

import de.jkamue.mqtt.MalformedPacketMqttException
import de.jkamue.mqtt.packet.PingreqPacket
import mqtt.parser.MQTTByteBuffer

internal object PingreqPacketParser {
    fun parsePingReqPacket(buffer: MQTTByteBuffer): PingreqPacket {
        if (buffer.remaining() != 0) throw MalformedPacketMqttException("PINGREQ Packet too large")
        return PingreqPacket
    }
}