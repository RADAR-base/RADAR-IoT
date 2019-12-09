package org.radarbase.iot.converter.avro.grovepi

import com.fasterxml.jackson.core.type.TypeReference
import org.radarbase.data.AvroRecordData
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.avro.AvroConverter
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.GrovePiAirQualitySensor
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory

class AirQualityAvroConverter(
    private val topicName: String = "grove_pi_air_quality",
    private val messageParser: Parser<String, List<GrovePiAirQualitySensor>> =
        JsonMessageParser(typeReference)
) :
    AvroConverter<ObservationKey, GrovePiAirQualitySensor> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, GrovePiAirQualitySensor> =
        AvroTopic(
            topicName, ObservationKey.getClassSchema(), GrovePiAirQualitySensor.getClassSchema(),
            ObservationKey::class.java, GrovePiAirQualitySensor::class.java
        )

    override fun convert(messages: List<String>):
            RecordData<ObservationKey, GrovePiAirQualitySensor> {

        val values: List<GrovePiAirQualitySensor> = messages.map {
            logger.debug("Parsing message: $it")
            messageParser.parse(it)
        }.flatten()

        logger.debug("Avro Values: ${values.map { it.toString() }}")

        return AvroRecordData<ObservationKey, GrovePiAirQualitySensor>(
            getAvroTopic(),
            AvroConverter.genericObservationKey,
            values
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AirQualityAvroConverter::class.java)

        private val typeReference = object : TypeReference<List<GrovePiAirQualitySensor>>() {}

    }
}