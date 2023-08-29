package org.radarbase.iot.commons.io.cache

interface Cache<K, V> {

    val size: Int

    fun set(key: K, value: V)

    fun get(key: K): V?

    fun remove(key: K): V?

    fun clear()

    fun toMap(): Map<K, V>

    fun stop()
}