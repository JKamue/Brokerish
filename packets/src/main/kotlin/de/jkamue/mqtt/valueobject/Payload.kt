package de.jkamue.mqtt.valueobject

import java.nio.ByteBuffer

@JvmInline
value class Payload(val payload: ByteBuffer)