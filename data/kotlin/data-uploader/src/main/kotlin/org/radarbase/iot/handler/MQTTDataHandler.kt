package org.radarbase.iot.handler

import kotlinx.coroutines.CoroutineExceptionHandler
import org.radarbase.iot.commons.exception.ConfigurationException
import org.radarbase.iot.commons.exception.LogAndContinueExceptionHandler
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.config.Configuration.Companion.SENSORS
import org.radarbase.iot.consumer.DataConsumer
import org.radarbase.iot.pubsub.connection.MQTTConnection
import org.slf4j.LoggerFactory

/**
 * Main Data handler class for initialising the [DataConsumer]s and subscribing to various
 * channels in Redis pub/sub.
 * It then listens for any new events (data from pub/sub) and forwards it to all the
 * [DataConsumer]s.
 */
class MQTTDataHandler : Handler {

    private lateinit var mqttCallback: RadarMQTTCallback

    private lateinit var mqttConnection: MQTTConnection

    private var isRunning: Boolean = false

    @Throws(ConfigurationException::class)
    override fun initialise() {
        logger.info("Configuration is : $CONFIGURATION")

        mqttConnection = MQTTConnection(CONFIGURATION.mqttConfig)
        mqttCallback = RadarMQTTCallback(SENSORS, mqttConnection)

        logger.info("MQTT connection is connected: {}", mqttConnection.isConnected())

        Thread.setDefaultUncaughtExceptionHandler(
            LogAndContinueExceptionHandler(
                "Exception in " +
                        "${MQTTDataHandler::class}"
            )
        )
    }

    @Throws(ConfigurationException::class)
    override fun start() {
        val topics = SENSORS.sensors.map { it.config.inputTopic }.toTypedArray()
        mqttConnection.getConnection().setCallback(mqttCallback)
        mqttConnection.getConnection().subscribe(topics, topics.map { CONFIGURATION.mqttConfig.qos }.toIntArray())
        isRunning = true
    }

    override fun stop() {
        logger.info("Stopping ${this::class.java.name}...")
        if (!isRunning) {
            logger.warn("The MQTT Data Handler is not running. Skipping stopping it")
            return
        }
        try {
            SENSORS.sensors.forEach {
                mqttConnection.getConnection().unsubscribe(it.config.inputTopic)
                it.consumerConverters.forEach { cc -> cc.dataConsumer.close() }
            }

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

        private val logger = LoggerFactory.getLogger(MQTTDataHandler::class.java)
    }
}