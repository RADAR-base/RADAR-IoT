package org.radarbase.iot.consumer

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.avro.SchemaValidationException
import org.radarbase.data.RecordData
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.converter.avro.AvroConverter
import org.radarbase.iot.sender.KafkaAvroDataSender
import org.slf4j.LoggerFactory
import java.io.IOException

open class RestProxyDataConsumer : DataConsumer<AvroConverter<*, *>> {
    private val kafkaDataSender: KafkaAvroDataSender

    constructor(
        uploadIntervalSeconds: Int,
        maxCacheSize: Int,
        kafkaDataSender: KafkaAvroDataSender
    ) : super(uploadIntervalSeconds, maxCacheSize) {
        this.kafkaDataSender = kafkaDataSender
    }

    constructor(
        uploadIntervalSeconds: Int,
        maxCacheSize: Int
    ) : super(uploadIntervalSeconds, maxCacheSize) {

        this.kafkaDataSender = KafkaAvroDataSender(
            authorizer = null,
            schemaUrl = CONFIGURATION.radarConfig.schemaRegistryUrl,
            kafkaUrl = CONFIGURATION.radarConfig.kafkaUrl
        )
    }

    override fun processData(messages: Map<AvroConverter<*, *>, List<String>>) {
        for ((k, v) in messages) {
            logger.debug("Converting and sending $v using $k")
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
        processData(this.cache.toMap()).also { this.cache.clear() }
        kafkaDataSender.close()
    }

    @Throws(IOException::class)
    open fun <K, V> sendToRestProxy(records: RecordData<K, V>) {
        try {
            kafkaDataSender.sendAll(records)
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
