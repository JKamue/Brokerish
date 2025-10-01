package de.jkamue.mqtt.packet

import de.jkamue.mqtt.ConnectReasonCode

data class ConnackPacket(
    val sessionPresent: Boolean,
    val connectReasonCode: ConnectReasonCode,

//    val sessionExpiryInterval: Interval?,
//    val receiveMaximum: ReceiveMaximum?,
//    val maximumQualityOfService: QualityOfService?,
//    val retainAvailable: Boolean?, // TODO
//    val maximumPacketSize: MaximumPacketSize?,
//    val assignedClientIdentifier: ClientId?, // TODO
//    val topicAliasMaximum: TopicAliasMaximum?,
//    val reasonString: String?,
//    val userProperties: UserProperties?,
//    val wildcardSubscriptionAvailable: Boolean?,
//    val subscriptionIdentifiersAvailable: Boolean?,
//    val sharedSubscriptionAvailable: Boolean?,
//    val serverKeepAlive: Interval?,
//    val responseInformation: String?,
//    val serverReference: String?,
//    val authenticationMethod: AuthenticationMethod?,
//    val authenticationData: ByteBuffer?,
) : Packet(ControlPacketType.CONNACK)