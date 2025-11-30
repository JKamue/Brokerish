package de.jkamue.mqtt.valueobject

import de.jkamue.mqtt.MalformedPacketMqttException

enum class QualityOfService(val number: Int) {
    // Values specified in section 4.3
    AT_MOST_ONCE_DELIVERY(0),
    AT_LEAST_ONCE_DELIVERY(1),
    EXACTLY_ONCE_DELIVERY(2);

    companion object {
        fun fromInt(number: Int) =
            when (number) {
                0 -> AT_MOST_ONCE_DELIVERY
                1 -> AT_LEAST_ONCE_DELIVERY
                2 -> EXACTLY_ONCE_DELIVERY
                // As described in 3.1.2.6 a value of 3 is Malformed
                else -> throw MalformedPacketMqttException("Unallowed Quality of Service requested")
            }
    }
}