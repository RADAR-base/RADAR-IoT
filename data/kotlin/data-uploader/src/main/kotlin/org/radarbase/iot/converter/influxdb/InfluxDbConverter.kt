package org.radarbase.iot.converter.influxdb

import org.influxdb.dto.Point
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.converter.Converter

interface InfluxDbConverter : Converter<List<String>, List<Point>> {

    companion object {
        val genericKeyMap: MutableMap<String, String> = mutableMapOf(
            Pair("projectId", CONFIGURATION.radarConfig.projectId),
            Pair("subjectId", CONFIGURATION.radarConfig.userId),
            Pair("sourceId", CONFIGURATION.radarConfig.sourceId)
        )
    }
}