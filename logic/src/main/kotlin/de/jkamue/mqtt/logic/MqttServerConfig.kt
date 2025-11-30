package de.jkamue.mqtt.logic

import de.jkamue.mqtt.valueobject.QualityOfService

interface MqttServerConfig {
    val maximumQualityOfService: QualityOfService
}