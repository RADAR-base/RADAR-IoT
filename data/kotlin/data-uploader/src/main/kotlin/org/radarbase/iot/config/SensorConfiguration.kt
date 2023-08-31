package org.radarbase.iot.config

import org.radarbase.iot.commons.exception.ConfigurationException
import org.radarbase.iot.consumer.DataConsumer
import org.radarbase.iot.converter.Converter

class SensorConfiguration(
    val consumerConverters: List<ConsumerConverter>,
    val config: Configuration.SensorConfig
)

class ConsumerConverter(
    val dataConsumer: DataConsumer<Converter<*, *>>,
    val converter: Converter<*, *>
)

data class TopicAndConsumer(
    val topic: String,
    val consumer: DataConsumer<*>
)

class Sensors(
    val config: Configuration
) {
    val sensors: List<SensorConfiguration> by lazy {
        config.sensorConfigs.map { sc ->
            SensorConfiguration(
                sc.converterClasses.map { cc ->
                    ConsumerConverter(
                        dataConsumer = config.dataConsumerConfigs.find { dc ->
                            dc.consumerName == cc.consumerName
                        }?.instance
                            ?: throw ConfigurationException(
                                "Data consumer ${cc.consumerName}" +
                                        " not found for sensor ${sc.sensorName}"
                            ),
                        converter = cc.instance
                    )
                },
                config = sc
            )
        }
    }

    fun sensorForTopic(topic: String): SensorConfiguration? = sensors.find { it.config.inputTopic == topic }
}