package de.jkamue.mqtt.valueobject

import java.nio.ByteBuffer

data class Will(
    val retain: Boolean,
    val qualityOfService: QualityOfService,
    val topic: Topic,
    val payload: Payload,
    val properties: WillProperties,
)

data class WillProperties(
    val willDelayInterval: Interval,
    val payloadFormat: PayloadFormat,
    val messageExpiryInterval: Interval?,
    val contentType: ContentType?,
    val responseTopic: Topic?,
    val correlationData: ByteBuffer?,
    val userProperties: UserProperties
)