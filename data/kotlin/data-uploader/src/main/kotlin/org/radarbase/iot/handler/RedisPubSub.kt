package org.radarbase.iot.handler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPubSub

class RedisPubSub(private val consumerAndConverterManager: ConsumerAndConverterManager) :
    JedisPubSub() {

    override fun onSubscribe(channel: String?, subscribedChannels: Int) {
        super.onSubscribe(channel, subscribedChannels)
        logger.info(
            """Subscribed to $channel channel.
            | Now total subscriptions are $subscribedChannels""".trimMargin()
        )
    }

    override fun onUnsubscribe(channel: String?, subscribedChannels: Int) {
        super.onUnsubscribe(channel, subscribedChannels)
        logger.info(
            """Unsubscribed from $channel channel.
            | Now total subscriptions are $subscribedChannels""".trimMargin()
        )
    }

    override fun onMessage(channel: String?, message: String?) {
        super.onMessage(channel, message)
        logger.debug("Received message: [${message}] from channel: [${channel}]")
        // Forward the message to all the dataConsumers by launching a coroutine
        GlobalScope.launch(RedisDataHandler.exceptionHandler) {
            consumerAndConverterManager.dataConsumerNameMap.forEach { (consumerName, consumer) ->
                try {
                    consumer.handleData(
                        message, consumerAndConverterManager
                            .converterForChannelAndConsumer(channel!!, consumerName)
                    )
                } catch (exc: NoSuchElementException) {
                    // TODO Maybe preempt this case as this could reduce performance as called
                    //  every time a message is received.
                    // No op as this consumer may not be registered on this channel and hence no
                    // converter found.
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RedisPubSub::class.java)
    }
}
