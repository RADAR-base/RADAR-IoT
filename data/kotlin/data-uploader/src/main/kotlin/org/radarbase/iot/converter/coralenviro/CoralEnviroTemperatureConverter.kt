package org.radarbase.iot.converter.coralenviro

import org.radarbase.data.AvroRecordData
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.AvroConverter
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.CoralEnviroTemperature
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory

class CoralEnviroTemperatureAvroConverter(
    private val topicName: String = "coral_enviro_temperature",
    private val messageParser: Parser<String, List<CoralEnviroTemperature>> =
        JsonMessageParser()
) :
    AvroConverter<ObservationKey, CoralEnviroTemperature> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, CoralEnviroTemperature> =
        AvroTopic(
            topicName, ObservationKey.getClassSchema(), CoralEnviroTemperature.getClassSchema(),
            ObservationKey::class.java, CoralEnviroTemperature::class.java
        )

    override fun convert(messages: List<String>): RecordData<ObservationKey, CoralEnviroTemperature> {

        val values: List<CoralEnviroTemperature> = messages.map {
            logger.debug("Parsing message: $it")
            messageParser.parse(it)
        }.flatten()

        return AvroRecordData<ObservationKey, CoralEnviroTemperature>(
            getAvroTopic(),
            AvroConverter.genericObservationKey,
            values
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CoralEnviroLightConverter::class.java)
    }
}