package org.radarbase.iot.handler

import org.radarbase.iot.commons.exception.ConfigurationException
import org.radarbase.iot.config.Configuration
import org.radarbase.iot.consumer.DataConsumer
import org.radarbase.iot.converter.Converter
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.system.exitProcess

class ConsumerAndConverterManager(
    private val configuration: Configuration
) {

    val dataConsumerNameMap: MutableMap<String, DataConsumer<Converter<*, *>>> =
        mutableMapOf()
    val channelConverterMap: MutableMap<String, MutableList<ConsumerConverterMap>> =
        mutableMapOf()

    data class ConsumerConverterMap(
        val consumerName: String,
        val converter: Converter<*, *>
    )

    init {

        for (dataConsumerConfig in configuration.dataConsumerConfigs) {
            try {
                dataConsumerNameMap[dataConsumerConfig.consumerName] =
                    Class.forName(dataConsumerConfig.consumerClass)?.constructors?.first { constructor ->
                        constructor.parameterCount == 2 && constructor.parameterTypes
                            .all { it == Int::class.java }
                    }?.newInstance(
                        dataConsumerConfig.uploadIntervalSeconds, dataConsumerConfig.maxCacheSize
                    )?.let { it as DataConsumer<Converter<*, *>> } ?: throw ConfigurationException(
                        "Cannot instantiate data " +
                                "consumer ${dataConsumerConfig.consumerClass}"
                    )
            } catch (exc: Exception) {
                logger.warn("Could not instantiate ${dataConsumerConfig.consumerClass}", exc)
            }
        }

        for (sensorConf in configuration.sensorConfigs) {
            logger.debug(sensorConf.toString())
            val listOfConverters = mutableListOf<ConsumerConverterMap>()
            for (converters in sensorConf.converterClasses) {
                val converter = Class.forName(converters.converterClass).constructors.first {
                    it.parameters.isEmpty()
                }.newInstance()?.let { it as Converter<*, *> } ?: throw
                ConfigurationException(
                    "Cannot instantiate data " +
                            "consumer ${converters.converterClass}"
                )
                if (dataConsumerNameMap.keys.contains(converters.consumerName)) {
                    listOfConverters.add(ConsumerConverterMap(converters.consumerName, converter))
                }
                channelConverterMap[sensorConf.inputTopic] = listOfConverters
            }
        }

        require(channelConverterMap.values.any { converterMap ->
            dataConsumerNameMap.keys.any { name ->
                converterMap.any {
                    it.consumerName == name
                }
            }
        }) {
            logger.error(
                "No valid association found between consumers and sensors. Please specify at least " +
                        "once sensor that a consumer is subscribed to in the configuration. " +
                        "Otherwise there is not point in running this application. Look for any " +
                        "other error before this one that may have caused this.",
                ConfigurationException("Invalid Configuration. The program will exit now...")
            )
            exitProcess(1)
        }

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

    fun getAllChannels(): List<String> = channelConverterMap.keys.toList()

    companion object {
        private val logger = LoggerFactory.getLogger(ConsumerAndConverterManager::class.java)
    }
}