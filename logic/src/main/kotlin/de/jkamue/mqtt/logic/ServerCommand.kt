package de.jkamue.mqtt.logic

import de.jkamue.mqtt.packet.Packet
import de.jkamue.mqtt.valueobject.ClientId
import kotlinx.coroutines.channels.SendChannel

/**
 * Sealed class representing all possible commands that can be sent to the [MqttServer] actor.
 */
sealed class ServerCommand
data class ClientConnected(val clientId: ClientId, val outgoing: SendChannel<Packet>) : ServerCommand()
data class ClientDisconnected(val clientId: ClientId) : ServerCommand()
data class PacketReceived(val clientId: ClientId, val packet: Packet) : ServerCommand()
