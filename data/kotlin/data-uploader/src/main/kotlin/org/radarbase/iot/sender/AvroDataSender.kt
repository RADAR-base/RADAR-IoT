package org.radarbase.iot.sender

import org.radarbase.data.RecordData
import org.radarbase.topic.AvroTopic

interface AvroDataSender {
    fun <K, V> send(key: K, value: V, topic: AvroTopic<K, V>)
    fun <K, V> sendAll(records: RecordData<K, V>, topic: AvroTopic<K, V>)
}