package org.radarbase.iot.handler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.radarbase.iot.config.Sensors
import org.radarbase.iot.pubsub.connection.MQTTConnection
import org.slf4j.LoggerFactory

class RadarMQTTCallback(val sensors: Sensors, val conn: MQTTConnection) :
    MqttCallback {
    override fun connectionLost(cause: Throwable?) {
        logger.warn("MQTT connection lost due to", cause)
        conn.getConnection()
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        // Forward the message to all the dataConsumers by launching a coroutine
        GlobalScope.launch(MQTTDataHandler.exceptionHandler) {

            topic?.let {
                sensors.sensorForTopic(topic)?.consumerConverters?.forEach {
                    try {
                        it.dataConsumer.handleData(String(message?.payload ?: return@launch), it.converter)
                    } catch (exc: NoSuchElementException) {
                        // TODO Maybe preempt this case as this could reduce performance as called
                        //  every time a message is received.
                        // No op as this consumer may not be registered on this channel and hence no
                        // converter found.
                    }
                }
            }
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        logger.info("Message with id ${token?.messageId} delivered to MQTT server.")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RadarMQTTCallback::class.java)
    }

}