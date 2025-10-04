import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

object BufferPool {
    private const val BUFFER_SIZE = 8192 // 8KB buffers

    private val pool = ConcurrentLinkedQueue<ByteBuffer>()

    fun lease(): ByteBuffer {
        val buffer = pool.poll() ?: ByteBuffer.allocate(BUFFER_SIZE)
        buffer.clear()
        return buffer
    }

    fun release(buffer: ByteBuffer) {
        buffer.clear()
        pool.offer(buffer)
    }
}
