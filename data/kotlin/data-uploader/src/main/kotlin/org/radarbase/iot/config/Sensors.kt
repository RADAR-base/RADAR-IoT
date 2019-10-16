package org.radarbase.iot.config

import org.radarbase.iot.sensor.CoralEnviroHumidity
import org.radarbase.iot.sensor.CoralEnviroTemperature
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey

object Sensors {
    fun coralEnviroHumiditySensorTopic(topicName: String) = AvroTopic(
        topicName,
        ObservationKey.getClassSchema(), CoralEnviroHumidity.getClassSchema(),
        ObservationKey::class.java, CoralEnviroHumidity::class.java
    )

    fun coralEnviroTemperatureTopic(topicName: String) = AvroTopic(
        topicName,
        ObservationKey.getClassSchema(), CoralEnviroTemperature.getClassSchema(),
        ObservationKey::class.java, CoralEnviroTemperature::class.java
    )

    // TODO("Add all other sensor topics too")
}