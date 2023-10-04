package org.radarbase.iot.commons.io.cache

/**
 * Uses Decorator pattern to extend the functionality of the underlying [Cache] [delegate] to
 * support LRU (Least Recently Used) cache replacement policy. You can provide an optional
 * consumer to consume the values when they are removed. This can help restrict the amount of
 * memory used.
 * More info on [LRU](https://en.wikipedia.org/wiki/Cache_replacement_policies#LRU)
 */
class LRUCache<K, V>(
    private val delegate: Cache<K, V>,
    private val maxSize: Int = DEFAULT_SIZE,
    private val consumer: ((K, V?) -> Unit)?
) :
    Cache<K, V> {

    /**
     * A Key Map to keep track of which keys are in the [delegate]. The values of each key is a
     * boolean representing whether a key is present is or not. By default we only insert the key
     * when it is present so it's always true but this may change in the future.
     */
    private val keyMap = object : LinkedHashMap<K, Boolean>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, Boolean>?): Boolean {
            val tooManyCachedItems = size > maxSize
            if (tooManyCachedItems) eldestKeyToRemove = eldest?.key
            return tooManyCachedItems
        }
    }

    private var eldestKeyToRemove: K? = null

    override val size: Int
        get() = delegate.size

    override fun set(key: K, value: V) {
        delegate.set(key, value)
        cycleKeyMap(key)
    }

    override fun get(key: K): V? {
        keyMap[key]
        return delegate.get(key)
    }

    override fun remove(key: K): V? {
        keyMap.remove(key)
        return delegate.remove(key)
    }

    override fun clear() {
        keyMap.clear()
        delegate.clear()
    }

    private fun cycleKeyMap(key: K) {
        keyMap[key] = PRESENT
        eldestKeyToRemove?.let { eldestKey ->
            consumer?.let { consumer ->
                consumer(key, delegate.remove(eldestKey))
            }
            eldestKeyToRemove = null
        }

    }

    override fun toMap(): Map<K, V> = delegate.toMap()

    companion object {
        const val DEFAULT_SIZE = 1000
        const val PRESENT = true
    }

    override fun stop() {
        delegate.stop()
    }
}