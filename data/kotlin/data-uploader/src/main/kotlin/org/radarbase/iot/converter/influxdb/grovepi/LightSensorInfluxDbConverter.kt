package org.radarbase.iot.converter.influxdb.grovepi

import com.fasterxml.jackson.core.type.TypeReference
import org.influxdb.dto.Point
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.influxdb.InfluxDbConverter
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.GrovePiLightSensor
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class LightSensorInfluxDbConverter(
    private val measurementName: String = "grovePiTempAndHumidity",
    private val parser: Parser<String,
            List<GrovePiLightSensor>> = JsonMessageParser(typeReference)
) : InfluxDbConverter {
    override fun convert(messages: List<String>): List<Point> {
        return messages.map { message ->
            parser.parse(message).map {
                Point.measurement(measurementName)
                    .time(it.getTime().toLong(), TimeUnit.SECONDS)
                    .addField("value", it.getValue())
                    .addField("resistance", it.getResistance())
                    .tag(InfluxDbConverter.genericKeyMap)
                    .build()
            }
        }.flatten()
    }

    companion object {

        private val logger =
            LoggerFactory.getLogger(LightSensorInfluxDbConverter::class.java)

        private val typeReference =
            object : TypeReference<List<GrovePiLightSensor>>() {}
    }
}
