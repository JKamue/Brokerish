import de.jkamue.mqtt.logic.MqttServerConfig
import de.jkamue.mqtt.valueobject.QualityOfService
import io.ktor.server.config.*

@kotlinx.serialization.Serializable
data class BrokerishConfig(
    override val maximumQualityOfService: QualityOfService
) : MqttServerConfig {
    companion object {
        fun create(config: ApplicationConfig): BrokerishConfig {
            val brokerishConfig = config.config("brokerish")
            return BrokerishConfig(
                maximumQualityOfService = brokerishConfig.property("maximumQualityOfService").getAs()
            )
        }
    }
}