package org.radarbase.iot.handler

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.radarbase.iot.commons.exception.ConfigurationException
import org.radarbase.iot.commons.exception.LogAndContinueExceptionHandler
import org.radarbase.iot.commons.util.SingletonHolder
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.consumer.DataConsumer
import org.radarbase.iot.pubsub.connection.RedisConnection
import org.radarbase.iot.pubsub.connection.RedisConnectionProperties
import org.radarbase.iot.pubsub.subscriber.RedisSubscriber
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPubSub

/**
 * Main Data handler class for initialising the [DataConsumer]s and subscribing to various
 * channels in Redis pub/sub.
 * It then listens for any new events (data from pub/sub) and forwards it to all the
 * [DataConsumer]s.
 */
class RedisDataHandler : JedisPubSub(),
    Handler {

    private var redisProperties: RedisConnectionProperties =
        CONFIGURATION.redisProperties ?: RedisConnectionProperties()

    private lateinit var consumerAndConverterManager: ConsumerAndConverterManager

    @Throws(ConfigurationException::class)
    override fun initialise() {
        logger.info("Configuration is : $CONFIGURATION")

        consumerAndConverterManager = ConsumerAndConverterManager(CONFIGURATION)

        Thread.setDefaultUncaughtExceptionHandler(
            LogAndContinueExceptionHandler(
                "Exception in " +
                        "${RedisDataHandler::class}"
            )
        )
    }

    @Throws(ConfigurationException::class)
    override fun start() {
        for (sensorConf in CONFIGURATION.sensorConfigs) {
            subscriberFactory.getInstance(redisProperties).subscribe(sensorConf.inputTopic, this)
            logger.info("Subscribed to ${sensorConf.inputTopic}")
        }
    }

    override fun stop() {
        logger.info("Stopping ${this::class.java.name}...")
        consumerAndConverterManager.getAllChannels().forEach {
            this.unsubscribe(it)
        }
        consumerAndConverterManager.dataConsumerNameMap.values.forEach { it.close() }
        logger.info("Gracefully stopped ${this::class.java.name}")
    }

    override fun onMessage(channel: String?, message: String?) {
        super.onMessage(channel, message)
        logger.debug("Received message: [${message}] from channel: [${channel}]")
        // Forward the message to all the dataConsumers by launching a coroutine
        GlobalScope.launch(exceptionHandler) {
            consumerAndConverterManager.dataConsumerNameMap.forEach { (consumerName, consumer) ->
                consumer.handleData(
                    message, consumerAndConverterManager
                        .converterForChannelAndConsumer(channel!!, consumerName)
                )
            }
        }
    }

    override fun onSubscribe(channel: String?, subscribedChannels: Int) {
        super.onSubscribe(channel, subscribedChannels)
        logger.info(
            """Subscribed to $channel channel.
            | Now total subscriptions are $subscribedChannels""".trimMargin()
        )
    }

    override fun onUnsubscribe(channel: String?, subscribedChannels: Int) {
        super.onUnsubscribe(channel, subscribedChannels)
        logger.info(
            """Unsubscribed from $channel channel.
            | Now total subscriptions are $subscribedChannels""".trimMargin()
        )
    }

    companion object {

        private val subscriberFactory =
            SingletonHolder<RedisSubscriber, RedisConnectionProperties> {
                RedisSubscriber(RedisConnection(it))
            }

        private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            logger.error("Caught $exception: ${exception.message}", exception)
        }

        private val logger = LoggerFactory.getLogger(RedisDataHandler::class.java)
    }

}