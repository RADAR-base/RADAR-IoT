package org.radarbase.iot.pubsub.subscriber

interface Subscriber {

    fun subscribe(channel: String, consumer: Any)
}