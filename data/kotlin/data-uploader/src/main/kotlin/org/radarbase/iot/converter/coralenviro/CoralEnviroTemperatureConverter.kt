package org.radarbase.iot.converter.coralenviro

import org.radarbase.data.AvroRecordData
import org.radarbase.data.RecordData
import org.radarbase.iot.converter.AvroConverter
import org.radarbase.iot.sensor.CoralEnviroTemperature
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey

class CoralEnviroTemperatureAvroConverter(
    private val topicName: String = "coral_enviro_temperature"
) :
    AvroConverter<ObservationKey, CoralEnviroTemperature> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, CoralEnviroTemperature> =
        AvroTopic(
            topicName, ObservationKey.getClassSchema(), CoralEnviroTemperature.getClassSchema(),
            ObservationKey::class.java, CoralEnviroTemperature::class.java
        )

    override fun convert(messages: List<String>): RecordData<ObservationKey, CoralEnviroTemperature> {

        val values: List<CoralEnviroTemperature> = messages.map {
            CoralEnviroTemperature.getDecoder().decode(it.byteInputStream())
        }
        return AvroRecordData<ObservationKey, CoralEnviroTemperature>(
            getAvroTopic(),
            AvroConverter.genericObservationKey,
            values
        )
    }
}