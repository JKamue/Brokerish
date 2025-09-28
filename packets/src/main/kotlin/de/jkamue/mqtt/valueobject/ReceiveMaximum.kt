package de.jkamue.mqtt.valueobject

import de.jkamue.mqtt.ProtocolErrorMqttException

@JvmInline
value class ReceiveMaximum(val value: Int) {
    init {
        // 3.1.2.11.3
        if (value == 0) throw ProtocolErrorMqttException("It is a Protocol Error to include the Receive Maximum value more than once or for it to have the value 0")
    }
}

