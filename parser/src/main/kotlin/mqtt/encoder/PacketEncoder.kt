package mqtt.encoder

import de.jkamue.mqtt.packet.ConnackPacket
import de.jkamue.mqtt.packet.Packet
import java.nio.ByteBuffer

object PacketEncoder {
    fun encodeScatter(packet: Packet): Array<ByteBuffer> {
        return when (packet) {
            is ConnackPacket -> ConnackEncoder.encodeScatter(packet)
            else -> throw NotImplementedError("Packet not implemented yet")
        }
    }
}