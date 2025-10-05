package de.jkamue.mqtt.valueobject

import de.jkamue.mqtt.ProtocolErrorMqttException

// specified in 3.8.3.1
enum class RetainHandlingOptions(val value: Int) {
    SEND_AT_TIME_OF_SUBSCRIBE(0),
    SEND_AT_SUBSCRIBE_IF_SUBSCRIPTION_NEW(1),
    DO_NOT_SEND_AT_TIME_OF_SUBSCRIBE(2);

    companion object {
        fun fromInt(number: Int) =
            when (number) {
                0 -> SEND_AT_TIME_OF_SUBSCRIBE
                1 -> SEND_AT_SUBSCRIBE_IF_SUBSCRIPTION_NEW
                2 -> DO_NOT_SEND_AT_TIME_OF_SUBSCRIBE
                // 3.8.3.1 - It is a Protocol Error to send a Retain Handling value of 3.
                else -> throw ProtocolErrorMqttException("Unallowed retain handling options")
            }
    }
}