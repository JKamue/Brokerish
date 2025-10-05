package mqtt.encoder

import de.jkamue.mqtt.packet.PublishPacket
import java.nio.ByteBuffer

object PublishEncoder {
    fun encodeScatter(packet: PublishPacket): Array<ByteBuffer> {
        val topicName = MqttEncoderHelpers.encodeUtf8StringUtf8Bytes(packet.topic.topic)

        val test = packet.payload.remaining()

        val payload = packet.payload.duplicate().rewind()
        val test2 = payload.remaining()

        val packetLength =
            1 + // header
                    1 + // length
                    topicName.size + // topic name
                    // Qos 0 -> no identifier 2 + // packet identifier
                    1 + // property length (0)
                    payload.remaining() // payload length

        val buffer = ByteBuffer.allocate(packetLength)
        buffer.put(0b00110000.toByte()) // control packet type + fixed dup / qos / retain
        MqttEncoderHelpers.encodeVariableByteIntegerToBuffer(packetLength - 2, buffer)
        buffer.put(topicName)
        // Qos 0 -> no identifier MqttEncoderHelpers.encodeTwoByteInt(packet.packetIdentifier, buffer)
        buffer.put(0b00000000.toByte())
        buffer.put(payload)

        buffer.flip()

        return arrayOf(buffer)
    }
}