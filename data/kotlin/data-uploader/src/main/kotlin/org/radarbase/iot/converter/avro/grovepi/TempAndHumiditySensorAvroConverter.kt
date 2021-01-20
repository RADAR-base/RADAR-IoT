package org.radarbase.iot.converter.avro.grovepi

import com.fasterxml.jackson.core.type.TypeReference
import org.radarbase.data.AvroRecordData
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.avro.AvroConverter
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.GrovePiTemperatureAndHumiditySensor
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory

class TempAndHumiditySensorAvroConverter(
    private val topicName: String = "radar_iot_grove_pi_temperature_and_humidity",
    private val messageParser: Parser<String, List<GrovePiTemperatureAndHumiditySensor>> =
        JsonMessageParser(typeReference)
) :
    AvroConverter<ObservationKey, GrovePiTemperatureAndHumiditySensor> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, GrovePiTemperatureAndHumiditySensor> =
        AvroTopic(
            topicName,
            ObservationKey.getClassSchema(),
            GrovePiTemperatureAndHumiditySensor.getClassSchema(),
            ObservationKey::class.java,
            GrovePiTemperatureAndHumiditySensor::class.java
        )

    override fun convert(messages: List<String>):
            RecordData<ObservationKey, GrovePiTemperatureAndHumiditySensor> {

        val values: List<GrovePiTemperatureAndHumiditySensor> = messages.map {
            logger.debug("Parsing message: $it")
            messageParser.parse(it)
        }.flatten()

        logger.debug("Avro Values: ${values.map { it.toString() }}")

        return AvroRecordData<ObservationKey, GrovePiTemperatureAndHumiditySensor>(
            getAvroTopic(),
            AvroConverter.genericObservationKey,
            values
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TempAndHumiditySensorAvroConverter::class.java)

        private val typeReference =
            object : TypeReference<List<GrovePiTemperatureAndHumiditySensor>>() {}

    }
}