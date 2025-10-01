package mqtt.encoder

import de.jkamue.mqtt.packet.ConnackPacket
import de.jkamue.mqtt.packet.DisconnectPacket
import de.jkamue.mqtt.packet.Packet
import de.jkamue.mqtt.packet.PingrespPacket
import mqtt.encoder.connack.ConnackEncoder
import java.nio.ByteBuffer

object PacketEncoder {
    fun encodeScatter(packet: Packet): Array<ByteBuffer> {
        return when (packet) {
            is ConnackPacket -> ConnackEncoder.encodeScatter(packet)
            is PingrespPacket -> PingrespEncoder.encodeScatter(packet)
            is DisconnectPacket -> DisconnectEncoder.encodeScatter(packet)
            else -> throw NotImplementedError("Packet not implemented yet")
        }
    }
}