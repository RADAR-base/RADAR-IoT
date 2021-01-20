package org.radarbase.iot.pubsub.subscriber

interface Subscriber<T> {

    fun subscribe(channel: Array<String>, consumer: T)
}