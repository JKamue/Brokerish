import de.jkamue.AsyncLogger.log
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

object BufferPool {
    private const val BUFFER_SIZE = 8192 // 8KB buffers

    private val pool = ConcurrentLinkedQueue<ByteBuffer>()
    private val leasedBuffers = AtomicInteger(0)
    private val allocatedBuffers = AtomicInteger(0)

    fun lease(): ByteBuffer {
        val buffer = pool.poll() ?: run {
            log("[Buffer Pool]: $allocatedBuffers allocated, $leasedBuffers leased, created new")
            allocatedBuffers.incrementAndGet()
            ByteBuffer.allocate(BUFFER_SIZE)
        }
        buffer.clear()
        leasedBuffers.incrementAndGet()
        log("[Buffer Pool]: $allocatedBuffers allocated, $leasedBuffers leased, leased")
        return buffer
    }

    fun release(buffer: ByteBuffer) {
        buffer.clear()
        pool.offer(buffer)
        leasedBuffers.decrementAndGet()
        log("[Buffer Pool]: $allocatedBuffers allocated, $leasedBuffers released")
    }
}
