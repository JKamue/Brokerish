package de.jkamue

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*

object AsyncLogger {
    // Channel to queue log messages as raw nanoTimes + message
    private data class LogEntry(val nanoTime: Long, val msg: String)

    private val logChannel = Channel<LogEntry>(capacity = Channel.UNLIMITED)

    // Coroutine scope for logging
    private val loggerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Launch a coroutine that consumes the channel and formats timestamps there
        loggerScope.launch {
            for (entry in logChannel) {
                val formattedTime = formatNanoTime(entry.nanoTime)
                println("[$formattedTime] ${entry.msg}")
            }
        }
    }

    // Format nanoTime as hh:mm:ss.millis.nanos (called only in logger coroutine)
    private fun formatNanoTime(nanoTime: Long): String {
        val hours = nanoTime / 3_600_000_000_000
        val minutes = (nanoTime % 3_600_000_000_000) / 60_000_000_000
        val seconds = (nanoTime % 60_000_000_000) / 1_000_000_000
        val millis = (nanoTime % 1_000_000_000) / 1_000_000
        val nanos = nanoTime % 1_000_000
        return String.format(Locale.US, "%02d:%02d:%02d.%03d.%03d", hours, minutes, seconds, millis, nanos)
    }

    // Non-blocking log function (only pushes raw data)
    fun log(msg: String) {
        val now = System.nanoTime()
        logChannel.trySend(LogEntry(now, msg)) // non-blocking, cheap
    }

    // Graceful shutdown
    suspend fun shutdown() {
        logChannel.close()
        loggerScope.coroutineContext[Job]?.join()
    }
}
