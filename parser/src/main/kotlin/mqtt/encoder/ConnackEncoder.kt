package mqtt.encoder

import de.jkamue.mqtt.packet.ConnackPacket
import java.nio.ByteBuffer

object ConnackEncoder {
    fun encodeScatter(packet: ConnackPacket): Array<ByteBuffer> {
        val fixedHeaderByte0: Byte = (2 shl 4).toByte()
        val connAckFlags: Byte = if (packet.sessionPresent) 0x01 else 0x00
        val reason: Byte = packet.connectReasonCode.value.toByte()

        // For the minimal example we have no properties.
        val propertiesLength = 0
        val propLenEncSize = MqttEncoderHelpers.variableByteIntSize(propertiesLength)
        val variableHeaderSize = 1 + 1 + propLenEncSize // flags + reason + property length
        val remainingLength = variableHeaderSize
        val remainingLenEncSize = MqttEncoderHelpers.variableByteIntSize(remainingLength)

        // Build small header part (fixed header + remaining length bytes + first part of var header)
        val headerBuf = ByteBuffer.allocate(1 + remainingLenEncSize + 2 + propLenEncSize)
        headerBuf.put(fixedHeaderByte0)
        MqttEncoderHelpers.encodeVariableByteIntegerToBuffer(remainingLength, headerBuf)
        headerBuf.put(connAckFlags)
        headerBuf.put(reason)
        MqttEncoderHelpers.encodeVariableByteIntegerToBuffer(propertiesLength, headerBuf)

        headerBuf.flip()

        return arrayOf(headerBuf)
    }
}
