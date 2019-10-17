package org.radarbase.iot.sender

import auth.Authorizer
import okhttp3.Headers
import org.radarbase.config.ServerConfig
import org.radarbase.data.RecordData
import org.radarbase.producer.BatchedKafkaSender
import org.radarbase.producer.rest.RestClient
import org.radarbase.producer.rest.RestSender
import org.radarbase.producer.rest.SchemaRetriever
import org.radarbase.topic.AvroTopic
import util.SingletonHolder

class KafkaAvroDataSender(val baseUrl: String, val authorizer: Authorizer?) : AvroDataSender {

    private val restSender: RestSender

    init {
        val kafkaUrl = "${baseUrl}/kafka"
        val schemaUrl = "${baseUrl}/schema"

        val client = RestClient.global().apply {
            server(ServerConfig(kafkaUrl))
            gzipCompression(true)
        }.build()

        val schemaRetriever = schemaRetrieverFactory.getInstance(ServerConfig(schemaUrl))

        restSender = RestSender.Builder().apply {
            httpClient(client)
            schemaRetriever(schemaRetriever)
            useBinaryContent(true)
            headers(authorizer?.getAuthHeader() ?: Headers.Builder().build())
        }.build()
    }

    override fun <K, V> send(key: K, value: V, topic: AvroTopic<K, V>) {
        kafkaSenderFactory.getInstance(restSender).sender(topic).use { topicSender ->
            topicSender.send(key, value)
        }
    }

    override fun <K, V> sendAll(records: RecordData<K, V>, topic: AvroTopic<K, V>) {
        kafkaSenderFactory.getInstance(restSender).sender(topic).use { topicSender ->
            topicSender.send(records)
        }
    }

    companion object {
        private const val KAFKA_SENDER_AGE_MS_DEFAULT = 60_000
        private const val KAFKA_SENDER_MAX_BATCH_SIZE = 1000
        private const val SCHEMA_RETRIEVER_CONNECTION_TIMEOUT = 30L

        val kafkaSenderFactory = SingletonHolder<BatchedKafkaSender, RestSender> {
            BatchedKafkaSender(it, KAFKA_SENDER_AGE_MS_DEFAULT, KAFKA_SENDER_MAX_BATCH_SIZE)
        }
        val schemaRetrieverFactory = SingletonHolder<SchemaRetriever, ServerConfig> {
            SchemaRetriever(it, SCHEMA_RETRIEVER_CONNECTION_TIMEOUT)
        }
    }
}