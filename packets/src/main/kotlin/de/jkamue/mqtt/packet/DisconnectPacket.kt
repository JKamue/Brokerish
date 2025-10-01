package de.jkamue.mqtt.packet

import de.jkamue.mqtt.DisconnectReasonCode


data class DisconnectPacket(
    val reasonCode: DisconnectReasonCode,

    // val sessionExpiry: Interval?,
    // val reason: String?,
    // val userProperty: MutableMap<String, MutableList<String>>,
    // val serverReference: String
) : Packet(ControlPacketType.DISCONNECT)