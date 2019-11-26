package org.radarbase.iot.converter.influxdb.coralenviro

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.influxdb.dto.Point
import org.radarbase.iot.commons.util.Parser
import org.radarbase.iot.converter.influxdb.InfluxDbConverter
import org.radarbase.iot.converter.messageparser.JsonMessageParser
import org.radarbase.iot.sensor.CoralEnviroTemperature
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class CoralEnviroTemperatureInfluxDbConverter(
    private val measurementName: String = "coralEnviroTemperature",
    private val parser: Parser<String,
            List<CoralEnviroTemperature>> = JsonMessageParser(typeReference)
) :
    InfluxDbConverter {

    override fun convert(messages: List<String>): List<Point> {
        return messages.map { message ->
            parser.parse(message).map {
                Point.measurement(measurementName)
                    .time(it.getTime().toLong(), TimeUnit.SECONDS)
                    .addField("value", it.getValue())
                    .tag(InfluxDbConverter.genericKeyMap)
                    .build()
            }
        }.flatten()
    }

    companion object {

        private val logger =
            LoggerFactory.getLogger(CoralEnviroHumidityInfluxDbConverter::class.java)

        private val objectMapper = ObjectMapper().also { it.registerModule(KotlinModule()) }

        private val typeReference = object : TypeReference<List<CoralEnviroTemperature>>() {}
    }

}