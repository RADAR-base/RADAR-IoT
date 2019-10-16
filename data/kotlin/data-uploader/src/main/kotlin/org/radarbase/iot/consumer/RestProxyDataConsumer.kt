package org.radarbase.iot.consumer

import org.radarbase.iot.sender.KafkaAvroDataSender
import java.time.Duration

open class RestProxyDataConsumer(
    private val uploadInterval: Duration,
    private val maxCacheSize: Int,
    private val kafkaDataSender: KafkaAvroDataSender
) : DataConsumer(uploadInterval, maxCacheSize) {
    override fun processData() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleData(message: String?) {
        handleDataInternal(message)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open fun sendToRestProxy() {
        TODO("Send messages in bulk to rest proxy")
    }
}
