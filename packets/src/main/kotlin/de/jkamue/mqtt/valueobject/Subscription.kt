package de.jkamue.mqtt.valueobject

data class Subscription(
    val topicFilter: TopicFilter,
    val options: SubscriptionOptions
)

data class SubscriptionOptions(
    val qualityOfService: QualityOfService,
    val noLocal: Boolean,
    val retainAsPublished: Boolean,
    val retainHandlingOption: RetainHandlingOptions,
)