package org.radarbase.iot.pubsub.subscriber

import org.radarbase.iot.pubsub.connection.RedisConnection
import redis.clients.jedis.JedisPubSub

class RedisSubscriber(private val conn: RedisConnection) :
    Subscriber<JedisPubSub> {

    init {
        // Test if the connection works. It will throw an error if it does not.
        check(conn.isConnected())
    }

    override fun subscribe(channel: String, consumer: JedisPubSub) {
        conn.getConnection().use { it.subscribe(consumer, channel) }
    }
}