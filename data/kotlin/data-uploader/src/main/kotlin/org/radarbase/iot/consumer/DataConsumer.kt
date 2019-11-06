package org.radarbase.iot.consumer

import org.radarbase.iot.commons.io.cache.Cache
import org.radarbase.iot.commons.io.cache.ExpirableCache
import org.radarbase.iot.commons.io.cache.LRUCache
import org.radarbase.iot.commons.io.cache.SimpleCache
import org.radarbase.iot.converter.Converter
import org.slf4j.LoggerFactory
import java.time.Duration

abstract class DataConsumer<T : Converter<*, *>>(
    private val uploadIntervalSeconds: Int,
    private val maxCacheSize: Int
) {
    protected var cache: Cache<T, MutableList<String>> = ExpirableCache(
        delegate = LRUCache(
            delegate = SimpleCache(),
            maxSize = maxCacheSize,
            consumer = null
        ),
        flushInterval = Duration.ofSeconds(uploadIntervalSeconds.toLong()),
        autoFlush = true,
        consumer = { m -> processData(m) }
    )

    open fun handleData(message: String?, converter: T) {
        logger.info("Got message: $message")
        message?.let {
            val messages = cache.get(converter) ?: mutableListOf()
            messages.add(message)
            cache.set(converter, messages)
        }
    }

    abstract fun processData(messages: Map<T, List<String>>)

    companion object {
        private val logger = LoggerFactory.getLogger(DataConsumer::class.java)
    }
}