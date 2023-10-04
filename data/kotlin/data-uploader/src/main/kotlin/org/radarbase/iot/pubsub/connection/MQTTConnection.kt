package org.radarbase.iot.pubsub.connection

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.influxdb.*
import org.radarbase.iot.config.Configuration
import org.slf4j.LoggerFactory

class MQTTConnection(private val mqttConfig: Configuration.MQTTConfig) :
    Connection<MqttClient> {

    private val mqttClient = MqttClient(mqttConfig.host, MQTTConnection::class.java.name)

    init {
        connect()
    }

    private fun connect() {
        val connOpts = MqttConnectOptions()
        connOpts.isCleanSession = false
        connOpts.userName = mqttConfig.username
        connOpts.password = mqttConfig.password.toCharArray()
        mqttClient.connect(connOpts)
        logger.info("Connected to MQTT client...")
    }

    @Throws(InfluxDBIOException::class)
    override fun getConnection(): MqttClient {
        if (!mqttClient.isConnected) {
            connect()
        }
        return mqttClient
    }

    override fun getConnectionPool(): Any {
        TODO("not implemented in the underlying MQTT client")
    }

    override fun isConnected(): Boolean = mqttClient.isConnected


    override fun close() {
        mqttClient.close()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MQTTConnection::class.java)
    }
}