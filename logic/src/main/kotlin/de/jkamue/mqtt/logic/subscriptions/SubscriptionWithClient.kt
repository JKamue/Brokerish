package de.jkamue.mqtt.logic.subscriptions

import de.jkamue.mqtt.valueobject.ClientId
import de.jkamue.mqtt.valueobject.Subscription

data class SubscriptionWithClient(
    val subscription: Subscription,
    val clientId: ClientId,
)