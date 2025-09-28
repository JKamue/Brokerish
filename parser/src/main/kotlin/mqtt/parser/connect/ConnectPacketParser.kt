package mqtt.parser.connect

import de.jkamue.mqtt.MalformedPacketMqttException
import de.jkamue.mqtt.packet.ConnectPacket
import de.jkamue.mqtt.valueobject.*
import mqtt.parser.MQTTByteBuffer
import mqtt.parser.connect.properties.ConnectPropertiesParser
import mqtt.parser.connect.will.WillPropertyParser

object ConnectPacketParser {
    fun parseConnectPacket(bytes: ByteArray): ConnectPacket {
        val buffer = MQTTByteBuffer.wrap(bytes)

        val protocolName = buffer.getEncodedString()
        val protocolVersion = buffer.getUnsignedByte()

        val connectFlags = buffer.getUnsignedByte()
        val userNameFlag = connectFlags and 0b10000000 != 0
        val passwordFlag = connectFlags and 0b01000000 != 0
        val willRetainFlag = connectFlags and 0b00100000 != 0
        val willQoS = QualityOfService.fromInt(connectFlags and 0b00011000 shr 3)
        val willFlag = connectFlags and 0b00000100 != 0
        val cleanStart = connectFlags and 0b00000010 != 0
        val reservedFlag = connectFlags and 0b00000001 != 0
        if (reservedFlag) throw MalformedPacketMqttException("Reserved Connect flag set MQTT-3.1.2-3")

        val keepAlive = Interval(buffer.getTwoByteInt())

        val connectPropertyLength = buffer.getVariableByteInteger()
        val connectPropertiesBuffer = buffer.getNextBytesAsBuffer(connectPropertyLength)
        val connectProperties = ConnectPropertiesParser.parseConnectProperties(connectPropertiesBuffer)

        val clientId = ClientId(buffer.getEncodedString())

        val will = parseWill(willFlag, buffer, willRetainFlag, willQoS)

        var username: Username? = null
        if (userNameFlag) {
            username = Username(buffer.getEncodedString())
        }

        var password: Password? = null
        if (passwordFlag) {
            password = Password(buffer.getEncodedString())
        }

        return ConnectPacket(
            protocolName = protocolName,
            protocolVersion = protocolVersion,
            username = username,
            password = password,
            cleanStart = cleanStart,
            keepAlive = keepAlive,
            clientId = clientId,
            properties = connectProperties,
            will = will
        )
    }

    private fun parseWill(
        willFlag: Boolean,
        buffer: MQTTByteBuffer,
        willRetainFlag: Boolean,
        willQoS: QualityOfService
    ): Will {
        var willProperties: WillProperties? = null
        var willTopic: Topic? = null
        var willPayload: Payload? = null
        if (willFlag) {
            val willLengthPropertiesLength = buffer.getVariableByteInteger()
            val willPropertiesBuffer = buffer.getNextBytesAsBuffer(willLengthPropertiesLength)
            willProperties = WillPropertyParser.parseConnectWillProperties(willPropertiesBuffer)
            willTopic = Topic(buffer.getEncodedString())
            willPayload = Payload(buffer.getEncodedCharBuffer())
        }
        val will = Will(
            retain = willRetainFlag,
            qualityOfService = willQoS,
            topic = willTopic
                ?: throw MalformedPacketMqttException("MQTT-3.1.3-11 - The Will Topic MUST be a UTF-8 Encoded String."),
            payload = willPayload ?: throw MalformedPacketMqttException("Will Topic missing"),
            properties = willProperties ?: throw MalformedPacketMqttException("Will Properties missing")
        )
        return will
    }

}