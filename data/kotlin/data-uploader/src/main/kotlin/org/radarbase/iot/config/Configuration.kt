package org.radarbase.iot.config

import org.radarbase.iot.commons.auth.PersistentOAuthStateStore
import org.radarbase.iot.commons.exception.ConfigurationException
import org.radarbase.iot.consumer.DataConsumer
import org.radarbase.iot.converter.Converter
import org.radarbase.iot.dataHandlers
import org.radarbase.iot.pubsub.connection.RedisConnectionProperties
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant


data class Configuration(
    val radarConfig: RadarConfig,
    val sensorConfigs: List<SensorConfig>,
    val dataConsumerConfigs: List<DataConsumerConfig>,
    val redisProperties: RedisConnectionProperties = RedisConnectionProperties(),
    val persistenceStoreproperties: PersistentOAuthStateStore.NitriteProperties?,
    val influxDbConfig: InfluxDbConfig?
) {

    data class DataConsumerConfig(
        val consumerClass: String,
        val maxCacheSize: Int,
        val uploadIntervalSeconds: Int,
        val consumerName: String
    ) {
        val instance: DataConsumer<Converter<*, *>> by lazy {
            try {
                Class.forName(consumerClass).constructors.first { constructor ->
                    constructor.parameterCount == 2 && constructor.parameterTypes
                        .all { it == Int::class.java }
                }?.newInstance(
                    uploadIntervalSeconds, maxCacheSize
                )?.let { it as DataConsumer<Converter<*, *>> }
                    ?: throw ConfigurationException(
                        "Cannot instantiate data consumer $consumerClass"
                                + "because cannot find suitable class with constructor parameters."
                    )
            } catch (exc: Exception) {
                logger.error("Could not instantiate $consumerClass", exc)
                throw exc
            }
        }
    }

    data class RadarConfig(
        val projectId: String,
        val userId: String,
        val sourceId: String,
        val baseUrl: String,
        val oAuthClientId: String?,
        val oAuthClientSecret: String?,
        val metaToken: String?,
        val schemaRegistryUrl: String = "$baseUrl/schema",
        val kafkaUrl: String = "$baseUrl/kafka",
        val sourceTypeModel: String = "RADAR-IoT",
        val sourceTypeProducer: String = "RADAR",
        val sourceTypeCatalogVersion: String = "1.0.0",
        val managementPortalPath: String = "managementportal"
    )

    data class SensorConfig(
        val sensorName: String,
        val inputTopic: String,
        val outputTopic: String?,
        val converterClasses: List<ConverterConfig>
    )

    data class ConverterConfig(
        val consumerName: String,
        val converterClass: String
    ) {
        val instance: Converter<*, *> by lazy {
            Class.forName(converterClass).constructors.first {
                it.parameters.isEmpty()
            }.newInstance()?.let { it as Converter<*, *> } ?: throw
            ConfigurationException(
                "Cannot instantiate data " +
                        "consumer $converterClass"
            )
        }
    }

    data class InfluxDbConfig(
        val url: String = "http://localhost:8086",
        val username: String = "root",
        val password: String = "root",
        val dbName: String = "radarIot",
        val retentionPolicyName: String = "radarIotRetentionPolicy",
        val retentionPolicyDuration: String = "1h",
        val retentionPolicyReplicationFactor: Int = 1
    )

    companion object {

        private var lastFetch: Instant = Instant.MIN
        private val refreshInterval = Duration.ofMinutes(15)

        private val logger = LoggerFactory.getLogger(this::class.java)

        private lateinit var configuration: Configuration

        // Should be used wherever configuration is needed. Provides a Singleton of the
        // Configuration. Also tries to fetch the config every refreshInterval if required.
        val CONFIGURATION: Configuration
            get() {
                if (lastFetch.plus(refreshInterval).isBefore(Instant.now()) &&
                    ConfigurationFetcher.ConfigFetcher.hasUpdates()
                ) {
                    configuration = ConfigurationFetcher.ConfigFetcher.fetchConfig()
                    dataHandlers.forEach {
                        it.apply {
                            if (it.isRunning()) {
                                // restart the data handlers after the config change
                                stop()
                                initialise()
                                start()
                            }
                        }
                    }
                    lastFetch = Instant.now()
                }
                return configuration
            }
    }
}