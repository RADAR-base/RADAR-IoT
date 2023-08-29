package org.radarbase.iot.commons.io.cache

import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

/**
 * Uses Decorator pattern to extend the functionality of the underlying [Cache] [delegate] to
 * support Expiring cache replacement policy. A [consumer] must be provided to consume the values
 * when they are removed. Setting [autoFlush] to <true> will enable a periodic flush even when
 * there are no operations. Otherwise the flushing is only performed during operations.
 * More info on (https://en.wikipedia.org/wiki/Cache_replacement_policies)
 */
class ExpirableCache<K, V>(
    private val delegate: Cache<K, V>,
    private val flushInterval: Duration = Duration.ofSeconds(DEFAULT_FLUSH_INTERVAL_SECONDS),
    private val autoFlush: Boolean = false,
    private val consumer: (Map<K, V>) -> Unit
) : Cache<K, V> {

    private var lastFlush = Instant.MIN

    private val timer = Timer(this::class.java.name, false)

    init {
        logger.info("Starting Expirable Cache with interval $flushInterval")
        if (autoFlush) {
            timer.scheduleAtFixedRate(
                flushInterval.toMillis(), flushInterval.toMillis()
            ) {
                logger.info("Running fixed rate timer for flushing...")
                if (isExpired()) {
                    flush()
                }
            }
        }
    }

    override val size: Int
        get() = delegate.size

    override fun set(key: K, value: V) {
        if (isExpired()) {
            flush()
        }
        delegate.set(key, value)
    }

    override fun get(key: K): V? {
        if (isExpired()) {
            flush()
        }
        return delegate.get(key)
    }

    override fun remove(key: K): V? {
        if (isExpired()) {
            flush()
        }
        return delegate.remove(key)
    }

    override fun clear() {
        delegate.clear()
    }

    override fun toMap(): Map<K, V> = delegate.toMap()

    @Synchronized
    private fun flush() {
        logger.info("Flushing Cache now...")
        if (delegate.size > 0) {
            val map = toMap()
            clear()
            consumer.invoke(map)
        }
        lastFlush = Instant.now()
    }

    override fun stop() {
        timer.cancel()
        timer.purge()
        delegate.stop()
    }

    @Synchronized
    private fun isExpired() = lastFlush.plus(flushInterval).isBefore(Instant.now())

    companion object {
        const val DEFAULT_FLUSH_INTERVAL_SECONDS = 100L

        private val logger = LoggerFactory.getLogger(ExpirableCache::class.java)
    }
}