package org.radarbase.iot.converter.avro

import org.radarbase.data.RecordData
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey

class GenericAvroConverter: AvroConverter<ObservationKey, Any> {
    override fun getAvroTopic(): AvroTopic<ObservationKey, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun convert(messages: List<String>): RecordData<ObservationKey, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}