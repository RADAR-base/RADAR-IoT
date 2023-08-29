package org.radarbase.iot.handler

import kotlinx.coroutines.CoroutineExceptionHandler
import org.radarbase.iot.commons.exception.ConfigurationException
import org.radarbase.iot.commons.exception.LogAndContinueExceptionHandler
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.consumer.DataConsumer
import org.radarbase.iot.pubsub.connection.RedisConnection
import org.radarbase.iot.pubsub.subscriber.RedisSubscriber
import org.slf4j.LoggerFactory

/**
 * Main Data handler class for initialising the [DataConsumer]s and subscribing to various
 * channels in Redis pub/sub.
 * It then listens for any new events (data from pub/sub) and forwards it to all the
 * [DataConsumer]s.
 */
class RedisDataHandler : Handler {

    private lateinit var consumerAndConverterManager: ConsumerAndConverterManager

    private lateinit var redisPubSub: RedisPubSub

    private lateinit var redisSubscriber: RedisSubscriber

    private var isRunning: Boolean = false

    @Throws(ConfigurationException::class)
    override fun initialise() {
        logger.info("Configuration is : $CONFIGURATION")

        consumerAndConverterManager = ConsumerAndConverterManager(CONFIGURATION)
        redisPubSub = RedisPubSub(consumerAndConverterManager)
        redisSubscriber = RedisSubscriber(RedisConnection(CONFIGURATION.redisProperties))

        logger.info("Redispubsub is subscribed: {}", redisPubSub.isSubscribed)

        Thread.setDefaultUncaughtExceptionHandler(
            LogAndContinueExceptionHandler(
                "Exception in " +
                        "${RedisDataHandler::class}"
            )
        )
    }

    @Throws(ConfigurationException::class)
    override fun start() {
        val topics = CONFIGURATION.sensorConfigs.map { it.inputTopic }.toTypedArray()
        redisSubscriber.subscribe(topics, redisPubSub)
        isRunning = true
    }

    override fun stop() {
        logger.info("Stopping ${this::class.java.name}...")
        if (!isRunning) {
            logger.warn("The Redis Data Handler is not running. Skipping stopping it")
            return
        }
        try {
            consumerAndConverterManager.getAllChannels().forEach {
                redisPubSub.unsubscribe(it)
            }
            consumerAndConverterManager.dataConsumerNameMap.values.forEach { it.close() }
            logger.info("Gracefully stopped ${this::class.java.name}")
        } catch (exc: UninitializedPropertyAccessException) {
            logger.info("The ${this::class.java.name} was not initialised properly.")
        } finally {
            isRunning = false
        }
    }

    override fun isRunning(): Boolean = isRunning

    companion object {

        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            logger.error("Caught $exception: ${exception.message}", exception)
        }

        private val logger = LoggerFactory.getLogger(RedisDataHandler::class.java)
    }
}