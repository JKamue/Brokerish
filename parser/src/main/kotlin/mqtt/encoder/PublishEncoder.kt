package mqtt.encoder

import de.jkamue.mqtt.packet.PublishPacket
import mqtt.parser.MQTTByteBuffer
import java.nio.ByteBuffer

object PublishEncoder {
    val ZERO_BUFFER = ByteBuffer.wrap(byteArrayOf(0)).asReadOnlyBuffer()

    fun encodeScatter(packet: PublishPacket): Array<ByteBuffer> {
        val topicName = packet.topic.value
        val payload = packet.payload.duplicate().rewind()

        val contentLength = 2 + // topic name length
                topicName.remaining() + // topic name
                // Qos 0 -> no identifier 2 + // packet identifier
                1 + // property length (0)
                payload.remaining() // payload length
        val lengthAfterHeader = MqttEncoderHelpers.variableByteIntegerLength(contentLength)

        val buffer =
            ByteBuffer.allocate(1 + lengthAfterHeader + 2) // 1 header + lengthAfterHeader + 2 topic length name
        buffer.put(0b00110000.toByte()) // control packet type + fixed dup / qos / retain
        MqttEncoderHelpers.encodeVariableByteIntegerToBuffer(contentLength, buffer)
        MqttEncoderHelpers.encodeTwoByteInt(topicName.remaining(), buffer)

        buffer.flip()

        MQTTByteBuffer.wrap(topicName).getString()

        return arrayOf(
            buffer,
            topicName,
            // Qos 0 -> no identifier but in the future MqttEncoderHelpers.encodeTwoByteInt(packet.packetIdentifier, buffer)
            ZERO_BUFFER,
            payload
        )
    }
}