package de.jkamue.mqtt

enum class QualityOfService(number: Int) {
    // Values specified in section 4.3
    AT_MOST_ONCE_DELIVERY(0),
    AT_LEAST_ONCE_DELIVERY(1),
    EXACTLY_ONCE_DELIVERY(2),
}