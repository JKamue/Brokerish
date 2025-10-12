package de.jkamue.mqtt.logic

import de.jkamue.mqtt.ConnectReasonCode
import de.jkamue.mqtt.DisconnectReasonCode
import de.jkamue.mqtt.logic.subscriptions.SubscriptionTree
import de.jkamue.mqtt.logic.subscriptions.SubscriptionWithClient
import de.jkamue.mqtt.packet.*
import de.jkamue.mqtt.valueobject.ClientId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class MqttServer(scope: CoroutineScope) {
    val commandChannel = Channel<ServerCommand>(Channel.UNLIMITED)
    private val clients = ConcurrentHashMap<ClientId, Client>()

    init {
        scope.launch(Dispatchers.Default) {
            for (command in commandChannel) {
                when (command) {
                    is ClientConnected -> {
                        val client = Client(command.clientId, command.outgoing)
                        val disconnectMsg = OutgoingMessage(DisconnectPacket(DisconnectReasonCode.SESSION_TAKEN_OVER))
                        clients[command.clientId]?.sendChannel?.send(disconnectMsg)
                        clients[command.clientId] = client
                    }

                    is ClientDisconnected -> {
                        SubscriptionTree.removeSubscriptionsFor(command.clientId)
                        println(SubscriptionTree)
                        clients.remove(command.clientId)
                        // TODO: Publish Will message by sending to other client channels
                    }

                    is PacketReceived -> {
                        handlePacket(command)
                    }
                }
            }
        }
    }

    private suspend fun handlePacket(command: PacketReceived) {
        val (clientId, packet, payloadManager) = command
        val client = clients[clientId] ?: run {
            // If client is not found, we must release the payload
            payloadManager.getReleaseAction()()
            return
        }

        when (packet) {
            is ConnectPacket -> {
                val response = ConnackPacket(
                    sessionPresent = false, // TODO: session handling
                    connectReasonCode = ConnectReasonCode.SUCCESS
                )
                payloadManager.getReleaseAction().invoke()
                client.sendChannel.send(OutgoingMessage(response))
            }

            is PingreqPacket -> {
                payloadManager.getReleaseAction().invoke()
                client.sendChannel.send(OutgoingMessage(PingrespPacket))
            }

            is SubscribePacket -> {
                val response = SubackPacket(
                    packetIdentifier = packet.packetIdentifier,
                    reasonCodes = packet.subscriptions.map { SubackReasonCode.GRANTED_QOS_0 }
                )
                client.sendChannel.send(OutgoingMessage(response))
                payloadManager.getReleaseAction().invoke()
                packet.subscriptions.forEach {
                    SubscriptionTree.addSubscription(
                        SubscriptionWithClient(it, clientId)
                    )
                }
                println(SubscriptionTree)
            }

            is PublishPacket -> {
                val subscriptions = SubscriptionTree.getClientsInterestedIn(packet.topic)
                if (subscriptions.isEmpty()) {
                    payloadManager.getReleaseAction().invoke()
                    return
                } else {
                    val sharedReleaseAction = payloadManager.getSharedReleaseAction(subscriptions.size)
                    val message = OutgoingMessage(packet, sharedReleaseAction)
                    subscriptions.forEach {
                        clients[it]?.sendChannel?.send(message) ?: sharedReleaseAction.invoke()
                    }
                }
            }

            else -> {
                // For any other packet type, we don't know what to do, so just release the resource.
                payloadManager.getReleaseAction().invoke()
            }
        }
    }
}
