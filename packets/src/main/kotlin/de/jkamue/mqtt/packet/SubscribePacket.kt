package de.jkamue.mqtt.packet

import de.jkamue.mqtt.valueobject.Subscription
import de.jkamue.mqtt.valueobject.UserProperties

data class SubscribePacket(
    val packetIdentifier: Int,

    val subscriptionIdentifier: Int?,
    val userProperties: UserProperties,

    val subscriptions: List<Subscription>
) : Packet(ControlPacketType.SUBSCRIBE)