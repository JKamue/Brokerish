package de.jkamue.mqtt.valueobject

import kotlin.time.Duration.Companion.seconds

@JvmInline
value class Interval(val duration: Int) {
    fun toDuration() = duration.seconds
}