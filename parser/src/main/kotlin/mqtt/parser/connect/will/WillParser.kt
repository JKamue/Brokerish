package mqtt.parser.connect.will

import de.jkamue.mqtt.MalformedPacketMqttException
import de.jkamue.mqtt.valueobject.Payload
import de.jkamue.mqtt.valueobject.Topic
import de.jkamue.mqtt.valueobject.Will
import mqtt.parser.MQTTByteBuffer
import mqtt.parser.connect.ConnectFlags
import mqtt.parser.createCopy

internal object WillParser {
    fun parse(buffer: MQTTByteBuffer, flags: ConnectFlags): Will? {
        if (!flags.willFlag) return null

        val willPropertiesLength = buffer.getVariableByteInteger()
        val willPropertiesBuffer = buffer.getNextBytesAsBuffer(willPropertiesLength)
        val willProperties = WillPropertyParser.parseConnectWillProperties(willPropertiesBuffer)

        // Create copy since will is kept in Memory
        val topicString = buffer.getBinaryData().createCopy()
        if (topicString.remaining() == 0) {
            throw MalformedPacketMqttException("MQTT-3.1.3-11 - The Will Topic MUST be a UTF-8 Encoded String.")
        }
        val willTopic = Topic(topicString)
        // Create copy since will is kept in Memory
        val willPayload = Payload(buffer.getBinaryData().createCopy())

        return Will(
            retain = flags.willRetain,
            qualityOfService = flags.willQos,
            topic = willTopic,
            payload = willPayload,
            properties = willProperties
        )
    }
}