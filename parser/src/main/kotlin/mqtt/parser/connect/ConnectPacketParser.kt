package mqtt.parser.connect

import de.jkamue.mqtt.packet.ConnectPacket
import de.jkamue.mqtt.valueobject.ClientId
import de.jkamue.mqtt.valueobject.Interval
import de.jkamue.mqtt.valueobject.Password
import de.jkamue.mqtt.valueobject.Username
import mqtt.parser.MQTTByteBuffer
import mqtt.parser.connect.properties.ConnectPropertiesParser
import mqtt.parser.connect.will.WillParser

object ConnectPacketParser {
    fun parseConnectPacket(bytes: ByteArray): ConnectPacket {
        val buffer = MQTTByteBuffer.wrap(bytes)

        // ------ Variable header ------
        val protocolName = buffer.getEncodedString()
        val protocolVersion = buffer.getUnsignedByte()

        val rawFlags = buffer.getUnsignedByte()
        val flags = ConnectFlags.fromByte(rawFlags)

        val keepAlive = Interval(buffer.getTwoByteInt())

        val connectPropertyLength = buffer.getVariableByteInteger()
        val connectPropertiesBuffer = buffer.getNextBytesAsBuffer(connectPropertyLength)
        val connectProperties = ConnectPropertiesParser.parseConnectProperties(connectPropertiesBuffer)

        // ------ Payload ------
        val clientId = ClientId(buffer.getEncodedString())

        val will = WillParser.parse(buffer, flags)

        val username = if (flags.userName) Username(buffer.getEncodedString()) else null
        val password = if (flags.password) Password(buffer.getEncodedString()) else null

        return ConnectPacket(
            protocolName = protocolName,
            protocolVersion = protocolVersion,
            username = username,
            password = password,
            cleanStart = flags.cleanStart,
            keepAlive = keepAlive,
            clientId = clientId,
            properties = connectProperties,
            will = will
        )
    }
}