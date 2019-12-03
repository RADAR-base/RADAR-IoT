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

/**
 * Main Data handler class for initialising the [DataConsumer]s and subscribing to various
 * channels in Redis pub/sub.
 * It then listens for any new events (data from pub/sub) and forwards it to all the
 * [DataConsumer]s.
 */
class RedisDataHandler : Handler {

    private var redisProperties: RedisConnectionProperties =
        CONFIGURATION.redisProperties ?: RedisConnectionProperties()

    private lateinit var consumerAndConverterManager: ConsumerAndConverterManager

    private lateinit var redisPubSub: RedisPubSub

    @Throws(ConfigurationException::class)
    override fun initialise() {
        logger.info("Configuration is : $CONFIGURATION")

        consumerAndConverterManager = ConsumerAndConverterManager(CONFIGURATION)
        redisPubSub = RedisPubSub(consumerAndConverterManager)

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
            GlobalScope.launch(exceptionHandler) {
                subscriberFactory.getInstance(redisProperties).subscribe(
                    sensorConf.inputTopic,
                    redisPubSub
                )
                logger.info("Subscribed to ${sensorConf.inputTopic}")
            }
        }
    }

    override fun stop() {
        logger.info("Stopping ${this::class.java.name}...")
        try {
            consumerAndConverterManager.getAllChannels().forEach {
                redisPubSub.unsubscribe(it)
            }
            consumerAndConverterManager.dataConsumerNameMap.values.forEach { it.close() }
            logger.info("Gracefully stopped ${this::class.java.name}")
        } catch (exc: UninitializedPropertyAccessException) {
            logger.info("The ${this::class.java.name} was not initialised properly.")
        }
    }

    companion object {

        private val subscriberFactory =
            SingletonHolder<RedisSubscriber, RedisConnectionProperties> {
                RedisSubscriber(RedisConnection(it))
            }

        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            logger.error("Caught $exception: ${exception.message}", exception)
        }

        private val logger = LoggerFactory.getLogger(RedisDataHandler::class.java)
    }
}