package org.radarbase.iot.pubsub.subscriber

import org.radarbase.iot.handler.DataHandler
import org.radarbase.iot.pubsub.connection.RedisConnection
import redis.clients.jedis.JedisPubSub

class RedisSubscriber(private val conn: RedisConnection, private val onMessage: (Any) -> Any) :
        Subscriber {

    override fun subscribe(channel: String, consumer: Any) {
        if (consumer !is JedisPubSub) {
            throw IllegalArgumentException("""Data Consumer should extend
                | JedisPubSub to work with redis""".trimMargin())
        }
        conn.getConnection().use { it.subscribe(consumer, channel) }
    }
}