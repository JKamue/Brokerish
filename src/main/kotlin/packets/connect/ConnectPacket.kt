package de.jkamue.packets.connect

import de.jkamue.packets.ClientId
import de.jkamue.packets.Interval
import de.jkamue.packets.Password
import de.jkamue.packets.Username

data class ConnectPacket(
    val protocolName: String,
    val protocolVersion: Int,

    val username: Username?,
    val password: Password?,

    val cleanStart: Boolean,
    val keepAlive: Interval,
    val clientId: ClientId,



    )