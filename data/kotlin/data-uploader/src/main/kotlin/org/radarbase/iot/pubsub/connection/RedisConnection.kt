package org.radarbase.iot.pubsub.connection

import org.radarbase.iot.util.SingletonHolder
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class RedisConnection(
    val redisConnectionProperties: RedisConnectionProperties
) : Connection {

    override fun getConnectionPool(): Any = jedisPoolFactory.getInstance(redisConnectionProperties)

    override fun getConnection() = jedisPoolFactory.getInstance(redisConnectionProperties).resource

    companion object {
        val jedisPoolFactory = SingletonHolder<JedisPool, RedisConnectionProperties> {
            JedisPool(JedisPoolConfig(), it.host, it.port, it.timeOut, it.password)
        }
    }
}

data class RedisConnectionProperties(
    val host: String,
    val port: Int,
    val timeOut: Int,
    val password: String
)