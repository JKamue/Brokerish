package de.jkamue.mqtt.logic

import de.jkamue.mqtt.valueobject.ClientId
import de.jkamue.mqtt.valueobject.Subscription
import java.util.concurrent.CopyOnWriteArrayList

object SubscriptionHandler {
    private val list = CopyOnWriteArrayList<Pair<Subscription, ClientId>>()

    fun addSubscription(subscription: Subscription, clientId: ClientId) {
        list.add(Pair(subscription, clientId))
    }

    fun removeSubscriptionsFor(client: Client) {
        for (subscription in client.subscriptions.value) {
            list.removeIf { it.second == client.id }
        }
    }
}