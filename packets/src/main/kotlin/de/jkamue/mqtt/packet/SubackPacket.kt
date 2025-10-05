package de.jkamue.mqtt.packet

class SubackPacket(
    val packetIdentifier: Int,
    // reasonString: String?,
    // userproperties: UserProperties,
    val reasonCodes: List<SubackReasonCode>
) : Packet(ControlPacketType.SUBACK)

// defined in 3.9.3
enum class SubackReasonCode(val value: Int) {
    // Success codes
    GRANTED_QOS_0(0),   // 0x00 - Granted QoS 0
    GRANTED_QOS_1(1),   // 0x01 - Granted QoS 1
    GRANTED_QOS_2(2),   // 0x02 - Granted QoS 2

    // Error codes
    UNSPECIFIED_ERROR(128),                      // 0x80 - The subscription is not accepted; generic failure.
    IMPLEMENTATION_SPECIFIC_ERROR(131),          // 0x83 - Server-specific rejection reason.
    NOT_AUTHORIZED(135),                         // 0x87 - Client is not authorized to subscribe.
    TOPIC_FILTER_INVALID(143),                   // 0x8F - Topic Filter is valid but not allowed for this Client.
    PACKET_IDENTIFIER_IN_USE(145),               // 0x91 - The Packet Identifier is already in use.
    QUOTA_EXCEEDED(151),                         // 0x97 - An imposed limit (implementation/admin) was exceeded.
    SHARED_SUBSCRIPTIONS_NOT_SUPPORTED(158),     // 0x9E - Shared Subscriptions not supported by Server.
    SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED(161), // 0xA1 - Subscription Identifiers not supported by Server.
    WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED(162);   // 0xA2 - Wildcard Subscriptions not supported by Server.

    companion object {
        fun fromValue(value: Int): SubackReasonCode? =
            entries.find { it.value == value }
    }
}
