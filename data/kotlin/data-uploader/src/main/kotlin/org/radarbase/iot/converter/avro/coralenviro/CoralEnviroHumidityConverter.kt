package org.radarbase.iot.converter.avro.coralenviro

import com.fasterxml.jackson.core.type.TypeReference
import org.radarbase.data.AvroRecordData
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.avro.AvroConverter
import org.radarbase.iot.converter.avro.AvroConverter.Companion.genericObservationKey
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.CoralEnviroHumidity
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory

class CoralEnviroHumidityConverter(
    private val topicName: String = "coral_enviro_humidity",
    private val messageParser: Parser<String, List<CoralEnviroHumidity>> =
        JsonMessageParser(typeReference)
) :
    AvroConverter<ObservationKey, CoralEnviroHumidity> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, CoralEnviroHumidity> =
        AvroTopic(
            topicName, ObservationKey.getClassSchema(), CoralEnviroHumidity.getClassSchema(),
            ObservationKey::class.java, CoralEnviroHumidity::class.java
        )

    override fun convert(messages: List<String>): RecordData<ObservationKey, CoralEnviroHumidity> {
        val values: List<CoralEnviroHumidity> = messages.map {
            logger.debug("Parsing message: $it")
            messageParser.parse(it)
        }.flatten()

        logger.debug("Avro Values: ${values.map { it.toString() }}")

        return AvroRecordData<ObservationKey, CoralEnviroHumidity>(
            getAvroTopic(),
            genericObservationKey,
            values
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CoralEnviroHumidityConverter::class.java)

        private val typeReference = object : TypeReference<List<CoralEnviroHumidity>>() {}
    }
}