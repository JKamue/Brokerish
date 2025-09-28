package de.jkamue.mqtt.valueobject

import de.jkamue.mqtt.PayloadFormatInvalidMqttException

enum class PayloadFormat(val value: Int) {
    UNSPECIFIED_BYTES(0),
    UTF8(1);

    companion object {
        // 3.1.3.2.3 - unspecified bytes, [...] is equivalent to not sending a Payload Format Indicator
        val DEFAULT = PayloadFormat.UNSPECIFIED_BYTES

        fun fromInt(value: Int): PayloadFormat {
            return when (value) {
                0 -> UNSPECIFIED_BYTES
                1 -> UTF8
                else -> throw PayloadFormatInvalidMqttException("Invalid payload format received")
            }
        }
    }
}