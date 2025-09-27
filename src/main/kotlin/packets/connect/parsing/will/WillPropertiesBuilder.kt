package de.jkamue.packets.connect.parsing.will

import de.jkamue.packets.ContentType
import de.jkamue.packets.Interval
import de.jkamue.packets.PayloadFormat
import de.jkamue.packets.Topic
import de.jkamue.packets.WillProperties
import java.nio.ByteBuffer

data class WillPropertiesBuilder(
    var willDelayInterval: Interval? = null,
    var payloadFormatIndicator: PayloadFormat? = null,
    var messageExpiryInterval: Interval? = null,
    var contentType: ContentType? = null,
    var responseTopic: Topic? = null,
    var correlationData: ByteBuffer? = null,
    val userProperties: MutableMap<String, MutableList<String>> = mutableMapOf()
) {
    fun build(): WillProperties {
        // 3.1.3.2.2 - If the Will Delay Interval is absent, the default value is 0
        val willDelay = willDelayInterval ?: Interval(0)

        // 3.1.3.2.3 - unspecified bytes, [...] is equivalent to not sending a Payload Format Indicator
        val payloadFormat = payloadFormatIndicator ?: PayloadFormat.UNSPECIFIED_BYTES

        return WillProperties(
            willDelayInterval = willDelay,
            payloadFormat = payloadFormat,
            messageExpiryInterval = messageExpiryInterval,
            contentType = contentType,
            responseTopic = responseTopic,
            correlationData = correlationData,
            userProperties = userProperties
        )
    }

    fun addUserProperty(key: String, value: String) {
        if (!userProperties.containsKey(key)) userProperties[key] = mutableListOf()
        userProperties[key]!!.add(value)
    }
}