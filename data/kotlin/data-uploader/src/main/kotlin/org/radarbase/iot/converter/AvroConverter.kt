package org.radarbase.iot.converter

import org.radarbase.data.RecordData
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey

interface AvroConverter<K, V> : Converter<List<String>, RecordData<K, V>> {
    fun getAvroTopic(): AvroTopic<K, V>

    companion object {
        val genericObservationKey = ObservationKey(
            CONFIGURATION.radarConfig.projectId, CONFIGURATION
                .radarConfig.userId, CONFIGURATION.radarConfig.sourceId
        )
    }
}
