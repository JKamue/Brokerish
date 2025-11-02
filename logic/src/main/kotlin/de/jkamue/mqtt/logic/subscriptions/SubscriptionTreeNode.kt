package de.jkamue.mqtt.logic.subscriptions

import de.jkamue.mqtt.valueobject.ClientId
import de.jkamue.mqtt.valueobject.Topic
import de.jkamue.mqtt.valueobject.TopicFilter
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

@JvmInline
value class NodeHasNoChildren(val value: Boolean)

class SubscriptionTreeNode {
    private val children = ConcurrentHashMap<ByteBuffer, SubscriptionTreeNode>()
    private val subscriptions = CopyOnWriteArraySet<SubscriptionWithClient>()

    companion object {
        private val PLUS_WILDCARD: ByteBuffer =
            ByteBuffer.wrap(byteArrayOf('+'.code.toByte())).asReadOnlyBuffer()
    }

    fun getSubscriptionsForTopic(remainingPath: Topic?): List<SubscriptionWithClient> {
        return if (remainingPath == null) {
            subscriptions.map { it }
        } else {
            val childName = remainingPath.firstSegment
            val directSubscriptions =
                children.get(childName)?.getSubscriptionsForTopic(remainingPath.remainingSegments) ?: emptyList()

            val wildcardSubscriptions =
                children.get(PLUS_WILDCARD)?.getSubscriptionsForTopic(remainingPath.remainingSegments)
                    ?: emptyList()
            return directSubscriptions + wildcardSubscriptions
        }
    }

    fun removeSubscription(clientId: ClientId, remainingPath: TopicFilter?): NodeHasNoChildren {
        val childName = remainingPath?.firstSegment
        val remainder = remainingPath?.remainingSegments
        if (childName != null) {
            val result = children.get(childName)?.removeSubscription(clientId, remainder)

            if (result != null && result.value) {
                children.remove(childName)
            }

        } else {
            subscriptions.removeIf { it.clientId == clientId }
        }
        return NodeHasNoChildren(children.isEmpty() && subscriptions.isEmpty())
    }

    fun addSubscription(subscription: SubscriptionWithClient, remainingPath: TopicFilter?) {
        if (remainingPath != null) {
            val remainder = remainingPath.remainingSegments
            createOrContactNode(subscription, remainingPath.firstSegment, remainder)
        } else {
            subscriptions.add(subscription)
        }
    }

    private fun createOrContactNode(
        subscription: SubscriptionWithClient,
        segmentName: ByteBuffer,
        remainingPath: TopicFilter?
    ) {
        val childNode =
            children.computeIfAbsent(segmentName.duplicate().rewind().asReadOnlyBuffer()) { SubscriptionTreeNode() }
        childNode.addSubscription(subscription, remainingPath)
    }

    override fun toString(): String {
        return toString(0)
    }

    private fun toString(pos: Int): String {
        val spaced = " ".repeat(pos)
        val newPos = pos + 1
        val subscriptions = subscriptions.map { spaced + "s-" + it.clientId + "\n" }
        val children = children.map { spaced + it.key.toUtf8String() + "\n" + it.value.toString(newPos) }
        return subscriptions.joinToString(separator = "") + children.joinToString(separator = "")
    }

    private fun ByteBuffer.toUtf8String(): String {
        val copy = this.duplicate()           // Don't modify original buffer
        val bytes = ByteArray(copy.remaining())
        copy.get(bytes)                       // Read only remaining bytes
        return String(bytes, StandardCharsets.UTF_8)
    }
}