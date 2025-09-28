package de.jkamue.mqtt.packet

import de.jkamue.mqtt.ClientId
import de.jkamue.mqtt.Interval
import de.jkamue.mqtt.Password
import de.jkamue.mqtt.Username

data class ConnectPacket(
    val protocolName: String,
    val protocolVersion: Int,

    val username: Username?,
    val password: Password?,

    val cleanStart: Boolean,
    val keepAlive: Interval,
    val clientId: ClientId,
)