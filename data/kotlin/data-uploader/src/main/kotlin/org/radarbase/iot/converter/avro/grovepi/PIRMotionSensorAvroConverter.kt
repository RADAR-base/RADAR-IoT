package org.radarbase.iot.converter.avro.grovepi

import com.fasterxml.jackson.core.type.TypeReference
import org.radarbase.data.AvroRecordData
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.avro.AvroConverter
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.GrovePiPIRMotionSensor
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory

class PIRMotionSensorAvroConverter(
    private val topicName: String = "radar_iot_grove_pi_pir_motion",
    private val messageParser: Parser<String, List<GrovePiPIRMotionSensor>> =
        JsonMessageParser(typeReference)
) :
    AvroConverter<ObservationKey, GrovePiPIRMotionSensor> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, GrovePiPIRMotionSensor> =
        AvroTopic(
            topicName,
            ObservationKey.getClassSchema(),
            GrovePiPIRMotionSensor.getClassSchema(),
            ObservationKey::class.java,
            GrovePiPIRMotionSensor::class.java
        )

    override fun convert(messages: List<String>):
            RecordData<ObservationKey, GrovePiPIRMotionSensor> {

        val values: List<GrovePiPIRMotionSensor> = messages.map {
            logger.debug("Parsing message: $it")
            messageParser.parse(it)
        }.flatten()

        logger.debug("Avro Values: ${values.map { it.toString() }}")

        return AvroRecordData<ObservationKey, GrovePiPIRMotionSensor>(
            getAvroTopic(),
            AvroConverter.genericObservationKey,
            values
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PIRMotionSensorAvroConverter::class.java)

        private val typeReference =
            object : TypeReference<List<GrovePiPIRMotionSensor>>() {}

    }
}