package de.jkamue.mqtt.valueobject

import de.jkamue.mqtt.ProtocolErrorMqttException

@JvmInline
value class MaximumPacketSize(val value: Int) {
    init {
        // 3.1.2.11.4
        if (value == 0) throw ProtocolErrorMqttException("It is a Protocol Error to include the Maximum Packet Size more than once, or for the value to be set to zero")
    }
}