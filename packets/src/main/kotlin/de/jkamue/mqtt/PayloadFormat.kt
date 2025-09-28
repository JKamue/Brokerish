package de.jkamue.mqtt

enum class PayloadFormat(val value: Int) {
    UNSPECIFIED_BYTES(0),
    UTF8(1);

    companion object {
        fun fromInt(value: Int): PayloadFormat {
            return when(value) {
                0 -> UNSPECIFIED_BYTES
                1 -> UTF8
                else -> throw PayloadFormatInvalidMqttException("Invalid payload format received")
            }
        }
    }
}