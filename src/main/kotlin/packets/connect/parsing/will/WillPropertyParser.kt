package de.jkamue.packets.connect.parsing.will

import de.jkamue.MalformedPacketMqttException
import de.jkamue.ProtocolErrorMqttException
import de.jkamue.packets.ContentType
import de.jkamue.packets.Interval
import de.jkamue.packets.MQTTByteBuffer
import de.jkamue.packets.PayloadFormat
import de.jkamue.packets.Topic
import de.jkamue.packets.WillProperties
import de.jkamue.packets.createCopy

object WillPropertyParser {

    fun parseConnectWillProperties(buffer: MQTTByteBuffer): WillProperties {
        val builder = WillPropertiesBuilder()
        while (buffer.remaining() > 0) {
            val propertyIdentifier = buffer.getUnsignedByte()
            WillPropertyHandlers.handlers[propertyIdentifier]?.invoke(buffer, builder)
                ?: throw MalformedPacketMqttException("Unknown will property: $propertyIdentifier")
        }

        return builder.build()
    }

    object WillPropertyHandlers {
        // Inline function prevents unboxing of value classes even though the function is generic
        // If propertName is passed as a String, String interpolation happens on every call
        // by sending it through a lambda interpolation is deferred to when the exception is actually thrown
        inline fun <T> T?.setOnce(newValue: T, inlineName: () -> String): T {
            if (this != null) throw ProtocolErrorMqttException("Will ${inlineName()} was present multiple times")
            return newValue
        }

        val handlers: Map<Int, (MQTTByteBuffer, WillPropertiesBuilder)-> Unit> by lazy {
            mapOf(
                WillPropertyIdentifier.WILL_DELAY_INTERVAL.identifier to { buffer, builder ->
                    builder.willDelayInterval = builder.willDelayInterval.setOnce(
                        Interval(buffer.getFourByteInt())
                    ) { "willDelayInterval" }
                },
                WillPropertyIdentifier.PAYLOAD_FORMAT_INDICATOR.identifier to { buffer, builder ->
                    builder.payloadFormatIndicator = builder.payloadFormatIndicator.setOnce(
                        PayloadFormat.fromInt(buffer.getUnsignedByte())
                    ) { "payloadFormatIndicator" }
                },
                WillPropertyIdentifier.MESSAGE_EXPIRY_INTERVAL.identifier to { buffer, builder ->
                    builder.messageExpiryInterval = builder.messageExpiryInterval.setOnce(
                        Interval(buffer.getFourByteInt())
                    ) { "messageExpiryInterval" }
                },
                WillPropertyIdentifier.CONTENT_TYPE.identifier to { buffer, builder ->
                    builder.contentType = builder.contentType.setOnce(
                        ContentType(buffer.getEncodedString())
                    ) { "contentType" }
                },
                WillPropertyIdentifier.RESPONSE_TOPIC.identifier to { buffer, builder ->
                    builder.responseTopic = builder.responseTopic.setOnce(
                        Topic(buffer.getEncodedString())
                    ) { "contentType" }
                },
                WillPropertyIdentifier.CORRELATION_DATA.identifier to { buffer, builder ->
                    builder.correlationData = builder.correlationData.setOnce(
                        // copy needs to be created otherwise the whole packet will be kept in memory
                        buffer.getBinaryData().createCopy()
                    ) { "correlationData" }
                },
                WillPropertyIdentifier.USER_PROPERTY.identifier to { buffer, builder ->
                    val key = buffer.getEncodedString()
                    val value = buffer.getEncodedString()
                    builder.addUserProperty(key, value)
                },
            )
        }
    }
}


// TODO: Original pattern here
//val builder = WillPropertiesBuilder()
//
//val handlers: Map<Int, (MQTTByteBuffer) -> Unit> = mapOf(
//    WillPropertyIdentifier.WILL_DELAY_INTERVAL.identifier to { b ->
//        builder.willDelayInterval = Interval(b.getFourByteInt())
//    },
//    WillPropertyIdentifier.PAYLOAD_FORMAT_INDICATOR.identifier to { b ->
//        builder.payloadIsUtf8 = b.getUnsignedByte() == 1
//    },
//    WillPropertyIdentifier.MESSAGE_EXPIRY_INTERVAL.identifier to { b ->
//        builder.messageExpiryInterval = Interval(b.getFourByteInt())
//    },
//    WillPropertyIdentifier.CONTENT_TYPE.identifier to { b ->
//        builder.contentType = ContentType(b.getEncodedString())
//    },
//    WillPropertyIdentifier.RESPONSE_TOPIC.identifier to { b ->
//        builder.responseTopic = Topic(b.getEncodedString())
//    },
//    WillPropertyIdentifier.CORRELATION_DATA.identifier to { b ->
//        builder.correlationData = b.getBinaryData()
//    },
//    WillPropertyIdentifier.USER_PROPERTY.identifier to { b ->
//        val key = b.getEncodedString()
//        val value = b.getEncodedString()
//        builder.userProperties[key] = value
//    }
//)
//

//
//return builder.build()