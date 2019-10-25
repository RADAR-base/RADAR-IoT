package org.radarbase.iot.converter.coralenviro

import org.radarbase.data.AvroRecordData
import org.radarbase.data.RecordData
import org.radarbase.iot.converter.AvroConverter
import org.radarbase.iot.converter.AvroConverter.Companion.genericObservationKey
import org.radarbase.iot.sensor.CoralEnviroHumidity
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey

class CoralEnviroHumidityAvroConverter(private val topicName: String = "coral_enviro_humidity") :
    AvroConverter<ObservationKey, CoralEnviroHumidity> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, CoralEnviroHumidity> =
        AvroTopic(
            topicName, ObservationKey.getClassSchema(), CoralEnviroHumidity.getClassSchema(),
            ObservationKey::class.java, CoralEnviroHumidity::class.java
        )

    override fun convert(messages: List<String>): RecordData<ObservationKey, CoralEnviroHumidity> {
        val values: List<CoralEnviroHumidity> = messages.map {
            CoralEnviroHumidity.getDecoder().decode(it.byteInputStream())
        }
        return AvroRecordData<ObservationKey, CoralEnviroHumidity>(
            getAvroTopic(),
            genericObservationKey,
            values
        )
    }
}