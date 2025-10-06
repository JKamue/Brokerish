package de.jkamue.mqtt.valueobject

import java.nio.ByteBuffer

@JvmInline
value class TopicFilter(val value: ByteBuffer)