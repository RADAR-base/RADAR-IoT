package org.radarbase.iot.consumer

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.radarbase.iot.config.Configuration
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.converter.influxdb.InfluxDbConverter
import org.radarbase.iot.pubsub.connection.InfluxDbConnection
import org.slf4j.LoggerFactory

class InfluxDbDataConsumer(
    private val uploadIntervalSeconds: Int,
    private val maxSize: Int
) : DataConsumer<InfluxDbConverter>(uploadIntervalSeconds, maxSize) {

    override fun processData(messages: Map<InfluxDbConverter, List<String>>) {
        if (influxDbConnection.isConnected()) {
            val conn = influxDbConnection.getConnection()
            var size = 0
            GlobalScope.launch(exceptionHadler) {
                messages.forEach { (k, v) ->
                    k.convert(v).forEach {
                        size++
                        conn.write(it)
                    }
                }
            }.invokeOnCompletion {
                logger.info("Successfully added $size records to InfluxDb")
                conn.close()
            }
        } else {
            logger.warn(
                "Could not process records as InfluxDb connection was not available. " +
                        "Adding back to cache."
            )
            GlobalScope.launch(exceptionHadler) {
                messages.forEach { (t, u) ->
                    u.forEach {
                        handleData(it, t)
                    }
                }
            }
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(InfluxDbDataConsumer::class.java)

        private val influxDbConnection = InfluxDbConnection(loadInfluxDbProperties())

        private val exceptionHadler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
            logger.warn("Error while writing recrods to InfluxDb", e)
        }

        fun loadInfluxDbProperties(): Configuration.InfluxDbConfig {
            return CONFIGURATION.influxDbConfig ?: Configuration.InfluxDbConfig()
        }
    }
}