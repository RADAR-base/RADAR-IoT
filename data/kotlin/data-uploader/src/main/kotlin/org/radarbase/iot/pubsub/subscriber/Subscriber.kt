package org.radarbase.iot.pubsub.subscriber

interface Subscriber<T> {

    fun subscribe(channel: String, consumer: T)
}