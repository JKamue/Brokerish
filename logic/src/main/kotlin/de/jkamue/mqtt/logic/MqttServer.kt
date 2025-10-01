package de.jkamue.mqtt.logic

import de.jkamue.mqtt.ConnectReasonCode
import de.jkamue.mqtt.DisconnectReasonCode
import de.jkamue.mqtt.ProtocolErrorMqttException
import de.jkamue.mqtt.packet.*
import de.jkamue.mqtt.valueobject.ClientId
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object MqttServer {

    private val clients = ConcurrentHashMap<ClientId, UUID>()

    fun processPacket(channelId: UUID, packet: Packet): List<Pair<UUID, Packet>> {
        return when (packet) {
            is PingreqPacket -> processPingreqPacket(channelId)
            is ConnectPacket -> throw ProtocolErrorMqttException("Connect packet sent incorrectly")
            else -> throw NotImplementedError("Packet not implemented yet")
        }
    }

    fun processPingreqPacket(channelId: UUID): List<Pair<UUID, Packet>> {
        return listOf(Pair(channelId, PingrespPacket))
    }

    data class ConnectPacketResult(
        val newChannelId: UUID,
        val connackPacket: Packet,
        val disconnect: Pair<UUID, Packet>?
    )

    fun processConnectPacket(packet: ConnectPacket): ConnectPacketResult {
        val channelId = UUID.randomUUID()
        val clientId = packet.clientId
        // If the ClientID represents a Client already connected to the Server, the Server sends a DISCONNECT packet to the existing Client with Reason Code of 0x8E (Session taken over) - MQTT-3.1.4-3
        val disconnect = clients[clientId]?.let { Pair(it, DisconnectPacket(DisconnectReasonCode.SESSION_TAKEN_OVER)) }
        clients[clientId] = channelId

        val responsePacket =
            ConnackPacket(
                sessionPresent = false,
                connectReasonCode = ConnectReasonCode.SUCCESS
            )

        return ConnectPacketResult(
            newChannelId = channelId,
            connackPacket = responsePacket,
            disconnect = disconnect
        )
    }
}