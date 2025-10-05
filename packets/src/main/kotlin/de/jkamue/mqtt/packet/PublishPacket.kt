package de.jkamue.mqtt.packet

import de.jkamue.mqtt.valueobject.*
import java.nio.ByteBuffer

data class PublishPacket(
    val dup: Boolean,
    val qualityOfService: QualityOfService,
    val retain: Boolean,
    val topic: Topic,
    val packetIdentifier: Int,

    val properties: PublishProperties,
    val payload: ByteBuffer
) : Packet(ControlPacketType.PUBLISH)

data class PublishProperties(
    val payloadFormat: PayloadFormat?,
    val messageExpiryInterval: Interval?,
    val topicAlias: Int?,
    val responseTopic: Topic?,
    val correlationData: ByteBuffer?,
    val userProperties: UserProperties,
    val subscriptionIdentifier: Int?,
    val contentType: ContentType?,
)