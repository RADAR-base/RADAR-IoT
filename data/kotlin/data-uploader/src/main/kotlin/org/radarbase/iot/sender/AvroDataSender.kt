package org.radarbase.iot.sender

import org.radarbase.data.RecordData
import org.radarbase.topic.AvroTopic
import java.io.Closeable

interface AvroDataSender : Closeable {
    fun <K, V> send(key: K, value: V, topic: AvroTopic<K, V>)
    fun <K, V> sendAll(records: RecordData<K, V>)
    fun isConnected(): Boolean
}