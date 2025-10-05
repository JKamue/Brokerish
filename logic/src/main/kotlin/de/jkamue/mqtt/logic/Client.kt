package de.jkamue.mqtt.logic

import de.jkamue.mqtt.valueobject.ClientId
import de.jkamue.mqtt.valueobject.Subscription
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow

class Client(
    val id: ClientId,
    val sendChannel: SendChannel<OutgoingMessage>,
    val subscriptions: MutableStateFlow<List<Subscription>> = MutableStateFlow<List<Subscription>>(emptyList())
)