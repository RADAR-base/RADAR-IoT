package org.radarbase.iot.consumer

import java.time.Duration

abstract class DataConsumer(private val uploadInterval: Duration, private val maxCacheSize: Int) {

    abstract fun handleData(message: String?)

    abstract fun processData()

    fun handleDataInternal(message: String?) {
        TODO("Store schema and serialised Avro message in cache")
    }
}