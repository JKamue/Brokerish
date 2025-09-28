package mqtt.parser.connect

import de.jkamue.mqtt.ClientId
import de.jkamue.mqtt.MalformedPacketMqttException
import mqtt.parser.MQTTByteBuffer
import de.jkamue.mqtt.WillProperties
import mqtt.parser.connect.ConnectPropertyIdentifier.*
import mqtt.parser.connect.will.WillPropertyParser
import java.nio.ByteBuffer
import java.nio.CharBuffer

object ConnectPacketParser {
    fun parseConnectPacket(bytes: ByteArray) {
        val buffer = MQTTByteBuffer.wrap(bytes)

        val protocolName = buffer.getEncodedString()
        val protocolVersion = buffer.getUnsignedByte()

        val connectFlags = buffer.getUnsignedByte()
        val userNameFlag = connectFlags and 0b10000000 != 0
        val passwordFlag = connectFlags and 0b01000000 != 0
        val willRetainFlag = connectFlags and 0b00100000 != 0
        val willQoS = connectFlags and 0b00011000 shr 3
        val willFlag = connectFlags and 0b00000100 != 0
        val cleanStart = connectFlags and 0b00000010 != 0
        val reservedFlag = connectFlags and 0b00000001 != 0
        if (reservedFlag) throw MalformedPacketMqttException("Reserved Connect flag set MQTT-3.1.2-3")

        val keepAlive = buffer.getTwoByteInt()

        val connectPropertyLength =  buffer.getVariableByteInteger()
        val connectPropertiesBuffer = buffer.getNextBytesAsBuffer(connectPropertyLength)
        val connectProperties = parseConnectProperties(connectPropertiesBuffer)

        val clientId = ClientId(buffer.getEncodedString())

        var willConfig: WillProperties? = null
        var willTopic = "no topic set"
        var willPayload = CharBuffer.wrap("")
        if (willFlag) {
            val willLengthPropertiesLength = buffer.getVariableByteInteger()
            val willPropertiesBuffer = buffer.getNextBytesAsBuffer(willLengthPropertiesLength)
            willConfig = WillPropertyParser.parseConnectWillProperties(willPropertiesBuffer)
            willTopic = buffer.getEncodedString()
            willPayload = buffer.getEncodedCharBuffer()
        }

        var userName = "no user"
        if (userNameFlag) {
            userName = buffer.getEncodedString()
        }

        var password = "no password"
        if (passwordFlag) {
            password = buffer.getEncodedString()
        }
    }



    fun parseConnectProperties(buffer: MQTTByteBuffer): Map<String, String> {
        val identifiers = mutableMapOf<String, String>()
        while (buffer.remaining() > 0) {
            val propertyIdentifier = buffer.getUnsignedByte()

            if (propertyIdentifier == SESSION_EXPIRY_INTERVAL.identifier) {
                val interval = buffer.getFourByteInt()
                identifiers[SESSION_EXPIRY_INTERVAL.name] = interval.toString()
                continue
            }

            if (propertyIdentifier == RECEIVE_MAXIMUM.identifier) {
                val receiveMaximum = buffer.getTwoByteInt()
                identifiers[RECEIVE_MAXIMUM.name] = receiveMaximum.toString()
                continue
            }

            if (propertyIdentifier == MAXIMUM_PACKET_SIZE.identifier) {
                val maximumPacketSize = buffer.getFourByteInt()
                identifiers[MAXIMUM_PACKET_SIZE.name] = maximumPacketSize.toString()
                continue
            }

            if (propertyIdentifier == TOPIC_ALIAS_MAXIMUM.identifier) {
                val topicAliasMaximum = buffer.getTwoByteInt()
                identifiers[TOPIC_ALIAS_MAXIMUM.name] = topicAliasMaximum.toString()
                continue
            }

            if (propertyIdentifier == REQUEST_RESPONSE_INFORMATION.identifier) {
                val requestResponseInformation = buffer.getUnsignedByte()
                identifiers[REQUEST_RESPONSE_INFORMATION.name] = requestResponseInformation.toString()
                continue
            }

            if (propertyIdentifier == REQUEST_PROBLEM_INFORMATION.identifier) {
                val requestProblemInformation = buffer.getUnsignedByte()
                identifiers[REQUEST_PROBLEM_INFORMATION.name] = requestProblemInformation.toString()
                continue
            }

            if (propertyIdentifier == ConnectPropertyIdentifier.USER_PROPERTY.identifier) {
                val key = buffer.getEncodedString()
                val value = buffer.getEncodedString()
                val text = "$key-$value;"
                if (!identifiers.containsKey(ConnectPropertyIdentifier.USER_PROPERTY.name)) {
                    identifiers[ConnectPropertyIdentifier.USER_PROPERTY.name] = text
                } else {
                    identifiers[ConnectPropertyIdentifier.USER_PROPERTY.name] += text
                }
                continue
            }

            if (propertyIdentifier == AUTHENTICATION_METHOD.identifier) {
                val authenticationmethod = buffer.getEncodedString()
                identifiers[AUTHENTICATION_METHOD.name] = authenticationmethod
                continue
            }

            if (propertyIdentifier == AUTHENTICATION_DATA.identifier) {
                val authenticationData = buffer.getBinaryData()
                identifiers[AUTHENTICATION_DATA.name] = authenticationData.toCommaSeparatedInts()
                continue
            }
        }

        return identifiers
    }

    fun ByteBuffer.toCommaSeparatedInts(): String =
        (0 until remaining()).joinToString(",") { get(it).toInt().and(0xFF).toString() }
}