package de.jkamue.mqtt.valueobject

import java.nio.ByteBuffer

data class Topic(val value: ByteBuffer) {

    val firstSegment: ByteBuffer by lazy {
        val buf = value.duplicate().rewind()

        while (buf.hasRemaining()) {
            if (buf.get() == '/'.code.toByte()) {
                val end = buf.position() - 1
                val segment = value.duplicate()
                segment.position(0)
                segment.limit(end)
                return@lazy segment.slice()
            }
        }

        val segment = value.duplicate()
        segment.clear()
        segment.slice()
    }

    val remainingSegments: Topic? by lazy {
        val first = firstSegment

        // If the first segment covers the whole buffer, no remainder exists
        if (first.remaining() == value.remaining()) return@lazy null

        // Start of remainder is right after the first segment + the '/' byte
        val remainderStart = first.remaining() + 1
        if (remainderStart >= value.limit()) return@lazy null

        val remainder = value.duplicate()
        remainder.position(remainderStart)
        remainder.limit(value.limit())

        Topic(remainder.slice())
    }
}

