package de.jkamue.mqtt.logic

import de.jkamue.mqtt.packet.Packet
import de.jkamue.mqtt.valueobject.ClientId
import kotlinx.coroutines.channels.SendChannel

/**
 * A message to be sent to a client, pairing the packet with a callback
 * to be executed after the packet has been successfully written to the socket.
 */
data class OutgoingMessage(
    val packet: Packet,
    val afterSend: () -> Unit = {}
)

/**
 * An interface provided by the connection handler to the logic module, allowing the logic
 * module to manage the lifecycle of a payload buffer without knowing the implementation details.
 */
interface PayloadManager {
    /**
     * For single-use payloads. Gets a simple action that will release the resource.
     */
    fun getReleaseAction(): () -> Unit

    /**
     * For payloads that are fanned out to multiple consumers.
     * Gets a new, shared release action that will release the resource only after
     * being called `count` times.
     */
    fun getSharedReleaseAction(count: Int): () -> Unit
}

/**
 * Sealed class representing all possible commands that can be sent to the [MqttServer] actor.
 */
sealed class ServerCommand
data class ClientConnected(val clientId: ClientId, val outgoing: SendChannel<OutgoingMessage>) : ServerCommand()
data class ClientDisconnected(val clientId: ClientId) : ServerCommand()
data class PacketReceived(
    val clientId: ClientId,
    val packet: Packet,
    val payloadManager: PayloadManager
) : ServerCommand()
