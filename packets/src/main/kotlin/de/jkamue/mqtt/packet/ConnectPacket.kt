package de.jkamue.mqtt.packet

import de.jkamue.mqtt.valueobject.*
import java.nio.ByteBuffer

data class ConnectPacket(
    val protocolName: String,
    val protocolVersion: Int,

    val username: Username?,
    val password: Password?,

    val cleanStart: Boolean,
    val keepAlive: Interval,
    val clientId: ClientId,

    val properties: ConnectProperties,
    val will: Will,
)

data class ConnectProperties(
    val sessionExpiry: Interval,
    val receiveMaximum: ReceiveMaximum,
    val maximumPacketSize: MaximumPacketSize?,
    val topicAliasMaximum: TopicAliasMaximum,
    val requestResponseInformation: RequestResponseInformation,
    val requestProblemInformation: RequestProblemInformation,
    val userProperties: UserProperties,
    val authenticationMethod: AuthenticationMethod?,
    val authenticationData: ByteBuffer?
)