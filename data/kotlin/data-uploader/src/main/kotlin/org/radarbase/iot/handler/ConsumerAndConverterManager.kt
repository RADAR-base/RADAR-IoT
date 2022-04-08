package org.radarbase.iot.handler

import org.radarbase.iot.commons.exception.ConfigurationException
import org.radarbase.iot.config.Configuration
import org.radarbase.iot.consumer.DataConsumer
import org.radarbase.iot.converter.Converter
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.system.exitProcess

class ConsumerAndConverterManager(
    configuration: Configuration
) {

    val dataConsumerNameMap: Map<String, DataConsumer<Converter<*, *>>>
    val channelConverterMap: Map<String, MutableList<ConsumerConverterMap>>

    data class ConsumerConverterMap(
        val consumerName: String,
        val converter: Converter<*, *>
    )

    init {
        val dataConsumerMap = mutableMapOf<String, DataConsumer<Converter<*, *>>>()

        configuration.dataConsumerConfigs.forEach {
            dataConsumerMap[it.consumerName] = it.instance
        }

        this.dataConsumerNameMap = dataConsumerMap
        logger.info(
            "Successfully initialised the Data Consumers: {}",
            this.dataConsumerNameMap.entries
        )

        val converterMap = mutableMapOf<String, MutableList<ConsumerConverterMap>>()


        for (sensorConf in configuration.sensorConfigs) {
            logger.debug(sensorConf.toString())
            val listOfConverters = mutableListOf<ConsumerConverterMap>()
            for (converters in sensorConf.converterClasses) {
                val converter = converters.instance
                if (dataConsumerNameMap.keys.contains(converters.consumerName)) {
                    listOfConverters.add(ConsumerConverterMap(converters.consumerName, converter))
                }
                converterMap[sensorConf.inputTopic] = listOfConverters
            }
        }

        require(converterMap.values.any { values ->
            dataConsumerNameMap.keys.any { name ->
                values.any {
                    it.consumerName == name
                }
            }
        }) {
            logger.error(
                "No valid association found between consumers and sensors. Please specify at least " +
                        "one sensor that a consumer is subscribed to in the configuration. " +
                        "Otherwise there is no point in running this application. Look for any " +
                        "other errors before this one that may have caused this.",
                ConfigurationException("Invalid Configuration. The program will exit now...")
            )
            exitProcess(1)
        }
        this.channelConverterMap = converterMap

        logger.info(
            "Successfully initialised the converters for sensor data: {}",
            this.channelConverterMap.entries
        )
    }

    fun dataConsumerForName(name: String): DataConsumer<*>? = dataConsumerNameMap[name]

    @Throws(NoSuchElementException::class)
    fun converterForChannelAndConsumer(channel: String, consumerName: String):
            Converter<*, *> {
        val convertersList = channelConverterMap.getOrElse(
            channel,
            { throw NoSuchElementException("No valid converter was found for this channel") }
        )

        return convertersList.first { it.consumerName == consumerName }.converter
    }

    fun getAllChannels(): Set<String> = channelConverterMap.keys

    companion object {
        private val logger = LoggerFactory.getLogger(ConsumerAndConverterManager::class.java)
    }
}