package org.radarbase.iot.sender

import org.apache.avro.SchemaValidationException
import org.radarbase.config.ServerConfig
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.auth.Authorizer
import org.radarbase.iot.commons.util.SingletonHolder
import org.radarbase.producer.AuthenticationException
import org.radarbase.producer.BatchedKafkaSender
import org.radarbase.producer.KafkaSender
import org.radarbase.producer.rest.RestClient
import org.radarbase.producer.rest.RestSender
import org.radarbase.producer.rest.SchemaRetriever
import org.radarbase.topic.AvroTopic
import java.io.IOException

open class KafkaAvroDataSender(
    private val baseUrl: String,
    private val authorizer: Authorizer?,
    private val kafkaUrl: String = "${baseUrl}/kafka",
    private val schemaUrl: String = "${baseUrl}/schema",
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

    private var kafkaSender: KafkaSender = getKafkaSender(getRestSender())

    @Throws(IOException::class, SchemaValidationException::class)
    override fun <K, V> send(key: K, value: V, topic: AvroTopic<K, V>) {
        checkAuth()
        kafkaSender
            .sender(topic).use { it.send(key, value) }
    }

    @Throws(IOException::class, SchemaValidationException::class)
    override fun <K, V> sendAll(records: RecordData<K, V>) {
        checkAuth()
        kafkaSender
            .sender(records.topic)
            .use { topicSender ->
                topicSender.send(records)
            }
    }

    @Throws(AuthenticationException::class)
    override fun isConnected(): Boolean {
        return kafkaSender.isConnected
    }

    private fun getRestSender(): RestSender {

        val builder = RestSender.Builder().apply {
            httpClient(client)
            schemaRetriever(schemaRetriever)
            useBinaryContent(true)
        }

        authorizer?.let {
            if (!authorizer.isLoggedIn()) {
                authorizer.initialise()
            }
            builder.headers(authorizer.getAuthHeader())
        }

        return builder.build()
    }


    private fun checkAuth() {
        authorizer?.let {
            when {
                !it.isLoggedIn() -> authorizer.initialise()
                it.isExpired().or(!isConnected()) ->
                    kafkaSender = getKafkaSender(getRestSender())
            }
        }
    }

    companion object {
        private const val KAFKA_SENDER_AGE_MS_DEFAULT = 60_000
        private const val KAFKA_SENDER_MAX_BATCH_SIZE = 1000
        private const val SCHEMA_RETRIEVER_CONNECTION_TIMEOUT = 30L

        private fun getKafkaSender(restSender: RestSender): BatchedKafkaSender =
            BatchedKafkaSender(
                restSender, KAFKA_SENDER_AGE_MS_DEFAULT,
                KAFKA_SENDER_MAX_BATCH_SIZE
            )

        val schemaRetrieverFactory = SingletonHolder<SchemaRetriever, ServerConfig> {
            SchemaRetriever(it, SCHEMA_RETRIEVER_CONNECTION_TIMEOUT)
        }
    }
}