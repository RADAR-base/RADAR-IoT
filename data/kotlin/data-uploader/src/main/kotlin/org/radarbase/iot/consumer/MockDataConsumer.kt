package org.radarbase.iot.consumer

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.avro.SchemaValidationException
import org.radarbase.data.RecordData
import org.radarbase.iot.converter.avro.AvroConverter
import org.slf4j.LoggerFactory
import java.io.IOException

open class MockDataConsumer(uploadIntervalSeconds: Int, maxCacheSize: Int) :
    DataConsumer<AvroConverter<*, *>>(uploadIntervalSeconds, maxCacheSize) {

    override fun processData(messages: Map<AvroConverter<*, *>, List<String>>) {
        for ((k, v) in messages) {
            logger.info("Converting and sending $v using $k")
            try {
                sendToRestProxy(k.convert(v))
            } catch (exc: IOException) {
                logger.warn(
                    "Messages for $k could not be sent. Adding to cache " +
                            "to be sent later...", exc
                )
                // TODO: Add to a persistent cache
                GlobalScope.launch(exceptionHadler) {
                    messages.forEach { (t, u) ->
                        u.forEach { handleData(it, t) }
                    }
                }
            }
        }
    }

    override fun close() {
        processData(this.cache.toMap()).also { this.cache.stop() }
    }

    @Throws(IOException::class)
    open fun <K, V> sendToRestProxy(records: RecordData<K, V>) {
        try {
            logger.info("Sending all records, n=${records.size()}")
        } catch (exc: SchemaValidationException) {
            logger.error(
                "Messages for ${records.topic} could not be sent due to schema " +
                        "validation failure. Discarding these messages.", exc
            )
        }
        logger.info("Successfully uploaded ${records.size()} records.")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)

        private val exceptionHadler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
            logger.warn("Error while uploading records to Rest proxy", e)
        }
    }
}
