package de.jkamue.mqtt.logic

import de.jkamue.mqtt.ConnectReasonCode
import de.jkamue.mqtt.DisconnectReasonCode
import de.jkamue.mqtt.packet.*
import de.jkamue.mqtt.valueobject.ClientId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class MqttServer(scope: CoroutineScope) {
    val commandChannel = Channel<ServerCommand>(Channel.UNLIMITED)
    private val clients = ConcurrentHashMap<ClientId, SendChannel<Packet>>()

    init {
        scope.launch(Dispatchers.Default) {
            for (command in commandChannel) {
                when (command) {
                    is ClientConnected -> {
                        clients[command.clientId]?.send(DisconnectPacket(DisconnectReasonCode.SESSION_TAKEN_OVER))
                        clients[command.clientId] = command.outgoing
                    }

                    is ClientDisconnected -> {
                        clients.remove(command.clientId)
                        // TODO: Publish Will message by sending to other client channels
                    }

                    is PacketReceived -> handlePacket(command.clientId, command.packet)
                }
            }
        }
    }

    private suspend fun handlePacket(clientId: ClientId, packet: Packet) {
        val outgoing = clients[clientId] ?: return
        val responses: List<Packet> = when (packet) {
            is ConnectPacket -> listOf(
                ConnackPacket(
                    sessionPresent = false, // TODO: session handling
                    connectReasonCode = ConnectReasonCode.SUCCESS
                )
            )

            is PingreqPacket -> listOf(PingrespPacket)
            else -> emptyList()
        }
        responses.forEach { outgoing.send(it) }
    }
}
