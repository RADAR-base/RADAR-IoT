package org.radarbase.iot.sender

import okhttp3.Headers
import org.apache.avro.SchemaValidationException
import org.radarbase.config.ServerConfig
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.auth.Authorizer
import org.radarbase.iot.commons.util.SingletonHolder
import org.radarbase.producer.BatchedKafkaSender
import org.radarbase.producer.rest.RestClient
import org.radarbase.producer.rest.RestSender
import org.radarbase.producer.rest.SchemaRetriever
import org.radarbase.topic.AvroTopic
import java.io.IOException

class KafkaAvroDataSender(
    val baseUrl: String,
    val authorizer: Authorizer?,
    val kafkaUrl: String = "${baseUrl}/kafka",
    val schemaUrl: String = "${baseUrl}/schema",
    private val client: RestClient = RestClient.global().apply {
        server(ServerConfig(kafkaUrl))
        gzipCompression(true)
    }.build(),
    private val schemaRetriever: SchemaRetriever = schemaRetrieverFactory.getInstance(
        ServerConfig(
            schemaUrl
        )
    )
) : AvroDataSender {

    init {
        if (authorizer != null && !authorizer.isLoggedIn()) {
            authorizer.initialise()
        }
    }

    @Throws(IOException::class, SchemaValidationException::class)
    override fun <K, V> send(key: K, value: V, topic: AvroTopic<K, V>) {
        kafkaSenderFactory.getInstance(getRestSender()).sender(topic).use { topicSender ->
            topicSender.send(key, value)
        }
    }

    @Throws(IOException::class, SchemaValidationException::class)
    override fun <K, V> sendAll(records: RecordData<K, V>) {
        kafkaSenderFactory.getInstance(getRestSender()).sender(records.topic).use { topicSender ->
            topicSender.send(records)
        }
    }

    override fun isConnected(): Boolean = kafkaSenderFactory
        .getInstance(getRestSender()).use {
            it.isConnected
        }

    private fun getRestSender() = RestSender.Builder().apply {
        httpClient(client)
        schemaRetriever(schemaRetriever)
        useBinaryContent(true)
        headers(authorizer?.getAuthHeader() ?: Headers.Builder().build())
    }.build()

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