package de.jkamue.mqtt.packet

import de.jkamue.mqtt.MalformedPacketMqttException

enum class ControlPacketType(val value: Int) {
    // Specified in Table 2-1 of MQTT 5.0 spec
    RESERVED(0) {
        override fun flagsAreValid(flags: Int) = throw IllegalArgumentException("Reserved Control Packet value used")
    },
    CONNECT(1) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    CONNACK(2) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    PUBLISH(3) {
        override fun flagsAreValid(flags: Int) = true
        // TODO
        // throw NotImplementedError("Exception for PUBLISH as specified in Table 2-2 not done yet")
    },
    PUBACK(4) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    PUBREC(5) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    PUBREL(6) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0010, flags)
    },
    PUBCOMP(7) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    SUBSCRIBE(8) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0010, flags)
    },
    SUBACK(9) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    UNSUBSCRIBE(10) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0010, flags)
    },
    UNSUBACK(11) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    PINGREQ(12) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    PINGRESP(13) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    DISCONNECT(14) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    },
    AUTH(15) {
        override fun flagsAreValid(flags: Int) = validateFlag(0b0000, flags)
    };

    // For validating [MQTT-2.1.3-1]
    abstract fun flagsAreValid(flags: Int): Boolean

    companion object {
        private val map = entries.associateBy { it.value }

        fun detect(value: Int): ControlPacketType {
            val type = value shr 4
            val flags = value and 0b00001111
            val controlType = map[type] ?: throw MalformedPacketMqttException("Unknown ControlPacketType: $type")
            if (!controlType.flagsAreValid(flags))
                throw MalformedPacketMqttException("Illegal flags $flags for ${controlType.name} MQTT-2.1.3-1")

            return controlType
        }

        private fun validateFlag(allowedValue: Int, actualValue: Int): Boolean {
            return allowedValue == actualValue
        }
    }

}