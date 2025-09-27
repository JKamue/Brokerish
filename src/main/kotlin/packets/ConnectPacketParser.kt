package de.jkamue.packets

import de.jkamue.MalformedPacketMqttException
import de.jkamue.packets.ConnectPropertyIdentifier.*
import de.jkamue.packets.WillPropertyIdentifier.*
import java.nio.ByteBuffer

class ConnectPacketParser(
    bytes: ByteArray
) {
    private val buffer = MQTTByteBuffer.wrap(bytes)

    fun parseConnectPacket() {
        val protocolName = buffer.getEncodedString()
        val procolVersion = buffer.getUnsignedByte()

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

        val clientId = buffer.getEncodedString()

        var willConfig: Map<String, String> = emptyMap()
        var willTopic = "no topic set"
        var willPayload = "no payload set"
        if (willFlag) {
            val willLengthPropertiesLength = buffer.getVariableByteInteger()
            val willPropertiesBuffer = buffer.getNextBytesAsBuffer(willLengthPropertiesLength)
            willConfig = parseConnectWill(willPropertiesBuffer)
            willTopic = buffer.getEncodedString()
            willPayload = buffer.getEncodedString()
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

    fun parseConnectWill(buffer: MQTTByteBuffer): Map<String, String> {
        val identifiers = mutableMapOf<String, String>()
        while (buffer.remaining() > 0) {
            val propertyIdentifier = buffer.getUnsignedByte()

            if (propertyIdentifier == WILL_DELAY_INTERVAL.identifier) {
                val willDelayInterval = buffer.getFourByteInt()
                identifiers[WILL_DELAY_INTERVAL.name] = willDelayInterval.toString()
                continue
            }

            if (propertyIdentifier == PAYLOAD_FORMAT_INDICATOR.identifier) {
                val payloadFormat = buffer.getUnsignedByte()
                identifiers[PAYLOAD_FORMAT_INDICATOR.name] = payloadFormat.toString()
                continue
            }

            if (propertyIdentifier == MESSAGE_EXPIRY_INTERVAL.identifier) {
                val messageExpiryInterval = buffer.getFourByteInt()
                identifiers[MESSAGE_EXPIRY_INTERVAL.name] = messageExpiryInterval.toString()
                continue
            }

            if (propertyIdentifier == CONTENT_TYPE.identifier) {
                val contentType = buffer.getEncodedString()
                identifiers[CONTENT_TYPE.name] = contentType
                continue
            }

            if (propertyIdentifier == RESPONSE_TOPIC.identifier) {
                val responseTopic = buffer.getEncodedString()
                identifiers[RESPONSE_TOPIC.name] = responseTopic
                continue
            }

            if (propertyIdentifier == CORRELATION_DATA.identifier) {
                val correlationData = buffer.getBinaryData()
                identifiers[CORRELATION_DATA.name] = correlationData.toCommaSeparatedInts()
                continue
            }

            if (propertyIdentifier == WillPropertyIdentifier.USER_PROPERTY.identifier) {
                val key = buffer.getEncodedString()
                val value = buffer.getEncodedString()
                val text = "$key-$value;"
                if (!identifiers.containsKey(WillPropertyIdentifier.USER_PROPERTY.name)) {
                    identifiers[WillPropertyIdentifier.USER_PROPERTY.name] = text
                } else {
                    identifiers[WillPropertyIdentifier.USER_PROPERTY.name] += text
                }
                continue
            }
        }

        return identifiers
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