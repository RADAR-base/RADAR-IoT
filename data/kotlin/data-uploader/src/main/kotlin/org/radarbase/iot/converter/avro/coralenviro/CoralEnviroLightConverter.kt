package org.radarbase.iot.converter.avro.coralenviro

import com.fasterxml.jackson.core.type.TypeReference
import org.radarbase.data.AvroRecordData
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.avro.AvroConverter
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.CoralEnviroLight
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory

class CoralEnviroLightConverter(
    private val topicName: String = "coral_enviro_light",
    private val messageParser: Parser<String, List<CoralEnviroLight>> =
        JsonMessageParser(typeReference)
) :
    AvroConverter<ObservationKey, CoralEnviroLight> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, CoralEnviroLight> =
        AvroTopic(
            topicName, ObservationKey.getClassSchema(), CoralEnviroLight.getClassSchema(),
            ObservationKey::class.java, CoralEnviroLight::class.java
        )

    override fun convert(messages: List<String>): RecordData<ObservationKey, CoralEnviroLight> {

        val values: List<CoralEnviroLight> = messages.map {
            logger.debug("Parsing message: $it")
            messageParser.parse(it)
        }.flatten()

        logger.debug("Avro Values: ${values.map { it.toString() }}")

        return AvroRecordData<ObservationKey, CoralEnviroLight>(
            getAvroTopic(),
            AvroConverter.genericObservationKey,
            values
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CoralEnviroLightConverter::class.java)

        private val typeReference = object : TypeReference<List<CoralEnviroLight>>() {}

    }
}