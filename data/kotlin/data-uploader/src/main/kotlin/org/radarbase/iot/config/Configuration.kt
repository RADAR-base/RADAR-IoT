package org.radarbase.iot.config

data class Configuration(
    val radarConfig: RadarConfig,
    val sensorConfigs: List<SensorConfig>,
    val dataConsumerConfigs: List<DataConsumerConfig>
) {

    data class DataConsumerConfig(
        val consumerClass: String, val maxCacheSize: Int,
        val uploadInterval: Int
    )

    data class RadarConfig(
        val projectId: String, val userId: String, val sourceId: String,
        val baseUrl: String, val oAuthClientId: String, val oAuthClientSecret: String
    )

    data class SensorConfig(
        val sensorName: String, val inputTopic: String, val outputTopic: String,
        val converterClass: String
    )

    companion object {

        const val ENV_CONFIG_LOCATION_PROPERTY = "RADAR_IOT_CONFIG_LOCATION"

        fun loadPropertiesFromFile(): Configuration {
            TODO("get config from file and create as Configuration instance")
        }
    }
}