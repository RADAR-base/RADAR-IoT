package org.radarbase.iot.converter.influxdb.grovepi

import com.fasterxml.jackson.core.type.TypeReference
import org.influxdb.dto.Point
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.influxdb.InfluxDbConverter
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.GrovePiAirQualitySensor
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class AirQualitySensorInfluxDbConverter(
    private val measurementName: String = "grovePiAirQuality",
    private val parser: Parser<String,
            List<GrovePiAirQualitySensor>> = JsonMessageParser(typeReference)
) : InfluxDbConverter {
    override fun convert(messages: List<String>): List<Point> {
        return messages.map { message ->
            parser.parse(message).map {
                Point.measurement(measurementName)
                    .time(it.getTime().toLong(), TimeUnit.SECONDS)
                    .addField("value", it.getValue())
                    .addField("airQuality", it.getAirQuality())
                    .tag(InfluxDbConverter.genericKeyMap)
                    .build()
            }
        }.flatten()
    }

    companion object {

        private val logger =
            LoggerFactory.getLogger(AirQualitySensorInfluxDbConverter::class.java)

        private val typeReference = object : TypeReference<List<GrovePiAirQualitySensor>>() {}
    }
}
