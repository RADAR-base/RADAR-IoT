package org.radarbase.iot.handler

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.radarbase.iot.commons.exception.ConfigurationException
import org.radarbase.iot.commons.exception.LogAndContinueExceptionHandler
import org.radarbase.iot.commons.util.SingletonHolder
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.consumer.DataConsumer
import org.radarbase.iot.converter.Converter
import org.radarbase.iot.converter.GenericAvroConverter
import org.radarbase.iot.pubsub.connection.RedisConnection
import org.radarbase.iot.pubsub.connection.RedisConnectionProperties
import org.radarbase.iot.pubsub.subscriber.RedisSubscriber
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPubSub

/**
 * Main Data handler class for initialising the [DataConsumer]s and subscribing to various channels.
 * It then listens for any new events (data from pub/sub) and forwards it to all the
 * [DataConsumer]s.
 */
class DataHandler : JedisPubSub(),
    Handler {

    private var dataConsumers: List<DataConsumer>
    private var redisProperties: RedisConnectionProperties
    private var channelConverterMap: MutableMap<String, Converter<*, *>>

    @Throws(ConfigurationException::class)
    override fun initialise() {
        dataConsumers = CONFIGURATION.dataConsumerConfigs.map { dataConsumerConfig ->
            Class.forName(dataConsumerConfig.consumerClass)?.constructors?.first { constructor ->
                constructor.parameterCount == 2 && constructor.parameterTypes
                    .all { it == Int::class.java }
            }?.newInstance(
                dataConsumerConfig.maxCacheSize, dataConsumerConfig
                    .uploadIntervalSeconds
            )?.let { it as DataConsumer } ?: throw ConfigurationException(
                "Cannot instantiate data " +
                        "consumer ${dataConsumerConfig.consumerClass}"
            )
        }.toList()

        Thread.setDefaultUncaughtExceptionHandler(
            LogAndContinueExceptionHandler(
                "Exception in " +
                        "${DataHandler::class}"
            )
        )
    }

    @Throws(ConfigurationException::class)
    override fun start() {
        CONFIGURATION.sensorConfigs.forEach { sensorConf ->
            val converter = Class.forName(sensorConf.converterClass).constructors.first {
                it.parameters.isEmpty()
            }.newInstance()?.let { it as Converter<*, *> } ?: throw
            ConfigurationException(
                "Cannot instantiate data " +
                        "consumer ${sensorConf.converterClass}"
            )
            channelConverterMap[sensorConf.inputTopic] = converter
            subscriberFactory.getInstance(redisProperties).subscribe(sensorConf.inputTopic, this)
        }
    }

    override fun stop() {
        channelConverterMap.forEach {
            this.unsubscribe(it.key)
        }
    }

    init {
        dataConsumers = emptyList()
        redisProperties = CONFIGURATION.redisProperties ?: RedisConnectionProperties()
        channelConverterMap = emptyMap<String, Converter<*, *>>().toMutableMap()
        logger.info("Configuration is : $CONFIGURATION")
    }


    override fun onMessage(channel: String?, message: String?) {
        super.onMessage(channel, message)
        logger.debug("Received message: [${message}] from channel: [${channel}]")
        // Forward the message to all the dataConsumers by launching a coroutine
        val converter = channelConverterMap.getOrDefault(channel!!, GenericAvroConverter())
        GlobalScope.launch(handler) {
            dataConsumers.forEach {
                it.handleData(message, converter)
            }
        }
    }

    override fun onSubscribe(channel: String?, subscribedChannels: Int) {
        super.onSubscribe(channel, subscribedChannels)
        logger.info(
            """Subscribed to ${channel} channel.
            | Now total subscriptions are ${subscribedChannels}""".trimMargin()
        )
    }

    override fun onUnsubscribe(channel: String?, subscribedChannels: Int) {
        super.onUnsubscribe(channel, subscribedChannels)
        logger.info(
            """Unsubscribed from ${channel} channel.
            | Now total subscriptions are ${subscribedChannels}""".trimMargin()
        )
    }

    companion object {

        private val subscriberFactory =
            SingletonHolder<RedisSubscriber, RedisConnectionProperties> {
                RedisSubscriber(RedisConnection(it))
            }

        private val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }

        private val logger = LoggerFactory.getLogger(DataHandler::class.java)
    }

}