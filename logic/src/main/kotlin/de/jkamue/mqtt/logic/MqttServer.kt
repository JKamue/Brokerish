package de.jkamue.mqtt.logic

import de.jkamue.mqtt.ConnectReasonCode
import de.jkamue.mqtt.ProtocolErrorMqttException
import de.jkamue.mqtt.packet.*
import de.jkamue.mqtt.valueobject.ClientId
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object MqttServer {

    private val clients: MutableSet<ClientId> = ConcurrentHashMap.newKeySet()

    fun processPacket(clientId: ClientId, packet: Packet): List<Pair<ClientId, Packet>> {
        return when (packet) {
            is PingreqPacket -> processPingreqPacket(clientId)
            is ConnectPacket -> throw ProtocolErrorMqttException("Connect packet sent incorrectly")
            else -> throw NotImplementedError("Packet not implemented yet")
        }
    }

    fun processPingreqPacket(clientId: ClientId): List<Pair<ClientId, Packet>> {
        return listOf(Pair(clientId, PingrespPacket))
    }

    fun processConnectPacket(packet: ConnectPacket): Pair<ClientId, Packet> {
        val clientId = if (clients.contains(packet.clientId)) {
            ClientId(UUID.randomUUID().toString())
        } else {
            packet.clientId
        }
        clients.add(clientId)

        val responsePacket =
            ConnackPacket(
                sessionPresent = false,
                connectReasonCode = ConnectReasonCode.SUCCESS,
                assignedClientIdentifier = clientId
            )

        return Pair(clientId, responsePacket)
    }
}