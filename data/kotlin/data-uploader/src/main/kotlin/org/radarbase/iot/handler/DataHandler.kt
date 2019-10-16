package org.radarbase.iot.handler

import org.radarbase.iot.consumer.DataConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPubSub
import kotlinx.coroutines.*

/**
 * Main Data handler class for initialising the [DataConsumer]s and subscribing to various channels.
 * It then listens for any new events (data from pub/sub) and forwards it to all the
 * [DataConsumer]s.
 */
class DataHandler(private val numConsumers: Int, private val numSubscribers: Int) : JedisPubSub() {
    private var dataConsumers: List<DataConsumer>

    init {
        dataConsumers = emptyList()


        // TODO("Create Subscriber for each sensor topic and pass to data Consumers")
    }


    override fun onMessage(channel: String?, message: String?) {
        super.onMessage(channel, message)
        logger.debug("Received message: [${message}] from channel: [${channel}]")
        // Forward the message to all the dataConsumers by launching a coroutine
        GlobalScope.launch {
            dataConsumers.forEach { it.handleData(message) }
        }
    }

    override fun onSubscribe(channel: String?, subscribedChannels: Int) {
        super.onSubscribe(channel, subscribedChannels)
        logger.info("""Subscribed to ${channel} channel.
            | Now total subscriptions are ${subscribedChannels}""".trimMargin())
    }

    override fun onUnsubscribe(channel: String?, subscribedChannels: Int) {
        super.onUnsubscribe(channel, subscribedChannels)
        logger.info("""Unsubscribed from ${channel} channel.
            | Now total subscriptions are ${subscribedChannels}""".trimMargin())
    }

    companion object {

        private val logger: Logger
            get() = LoggerFactory.getLogger(DataHandler::class.java)
    }

}