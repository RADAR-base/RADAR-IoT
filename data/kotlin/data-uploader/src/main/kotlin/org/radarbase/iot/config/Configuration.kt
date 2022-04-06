package org.radarbase.iot.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.radarbase.iot.commons.auth.PersistentOAuthStateStore
import org.radarbase.iot.commons.exception.ConfigurationException
import org.radarbase.iot.pubsub.connection.RedisConnectionProperties
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


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
    )

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
        val converterClasses: List<Converters>
    )

    data class Converters(
        val consumerName: String,
        val converterClass: String
    )

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

        private val logger = LoggerFactory.getLogger(this::class.java)

        const val ENV_CONFIG_LOCATION_PROPERTY = "RADAR_IOT_CONFIG_LOCATION"
        const val CONFIG_FILE_NAME_DEFAULT = "radar_iot_config.yaml"
        private val configFilePath: String by lazy {
            System.getenv(ENV_CONFIG_LOCATION_PROPERTY) ?: "/radar-iot/${CONFIG_FILE_NAME_DEFAULT}"
        }

        @Throws(ConfigurationException::class)
        internal fun loadPropertiesFromFile(): Configuration {
            var inputStream: InputStream
            try {
                inputStream = FileInputStream(File(configFilePath))
            } catch (e: Exception) {
                logger.warn(
                    "Could not load configuration from the File Path: ${configFilePath}." +
                            " Trying to load from the Classpath..."
                )
                try {
                    inputStream = CONFIG_FILE_NAME_DEFAULT.tryLoadFromClasspath()
                } catch (exc: Exception) {
                    throw ConfigurationException("Could not load Configuration From ClassPath.")
                }
            }

            val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
            mapper.registerModule(KotlinModule()) // Enable Kotlin support
            return inputStream.use {
                mapper.readValue(it, Configuration::class.java)
            }
        }

        private fun String.tryLoadFromClasspath(): InputStream {
            return this@Companion::class.java
                .classLoader
                .getResourceAsStream(this)!!
        }

        // Should be used wherever configuration is needed. Provides a Singleton of the
        // Configuration using lazy init
        val CONFIGURATION: Configuration by lazy {
            loadPropertiesFromFile()
        }
    }
}