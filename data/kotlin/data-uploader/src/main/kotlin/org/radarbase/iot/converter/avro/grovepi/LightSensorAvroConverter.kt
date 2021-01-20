package org.radarbase.iot.converter.avro.grovepi

import com.fasterxml.jackson.core.type.TypeReference
import org.radarbase.data.AvroRecordData
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.avro.AvroConverter
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.GrovePiLightSensor
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory

class LightSensorAvroConverter(
    private val topicName: String = "radar_iot_grove_pi_light",
    private val messageParser: Parser<String, List<GrovePiLightSensor>> =
        JsonMessageParser(typeReference)
) :
    AvroConverter<ObservationKey, GrovePiLightSensor> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, GrovePiLightSensor> =
        AvroTopic(
            topicName,
            ObservationKey.getClassSchema(),
            GrovePiLightSensor.getClassSchema(),
            ObservationKey::class.java,
            GrovePiLightSensor::class.java
        )

    override fun convert(messages: List<String>):
            RecordData<ObservationKey, GrovePiLightSensor> {

        val values: List<GrovePiLightSensor> = messages.map {
            logger.debug("Parsing message: $it")
            messageParser.parse(it)
        }.flatten()

        logger.debug("Avro Values: ${values.map { it.toString() }}")

        return AvroRecordData<ObservationKey, GrovePiLightSensor>(
            getAvroTopic(),
            AvroConverter.genericObservationKey,
            values
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LightSensorAvroConverter::class.java)

        private val typeReference =
            object : TypeReference<List<GrovePiLightSensor>>() {}

    }
}