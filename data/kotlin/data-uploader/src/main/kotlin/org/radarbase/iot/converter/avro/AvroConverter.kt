package org.radarbase.iot.converter.avro

import org.radarbase.data.RecordData
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.converter.Converter
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey

interface AvroConverter<K, V> :
    Converter<List<String>, RecordData<K, V>> {
    fun getAvroTopic(): AvroTopic<K, V>

    companion object {
        val genericObservationKey = ObservationKey(
            CONFIGURATION.radarConfig.projectId, CONFIGURATION
                .radarConfig.userId, CONFIGURATION.radarConfig.sourceId
        )
    }
}
