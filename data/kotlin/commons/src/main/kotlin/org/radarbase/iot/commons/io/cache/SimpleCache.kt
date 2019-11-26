package org.radarbase.iot.commons.io.cache

class SimpleCache<K, V> : Cache<K, V> {

    private val cache: MutableMap<K, V> = mutableMapOf()

    override val size: Int
        get() = cache.size

    override fun set(key: K, value: V) {
        cache[key] = value
    }

    override fun get(key: K): V? = cache[key]

    override fun remove(key: K): V? = cache.remove(key)

    override fun clear() = cache.clear()

    override fun toMap(): Map<K, V> = HashMap<K, V>(cache)
}