package org.radarbase.iot.consumer

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.avro.SchemaValidationException
import org.radarbase.config.ServerConfig
import org.radarbase.data.RecordData
import org.radarbase.iot.commons.auth.ManagementPortalAuthorizer
import org.radarbase.iot.commons.auth.MetaTokenLoginStrategy
import org.radarbase.iot.commons.auth.PersistentOAuthStateStore
import org.radarbase.iot.commons.managementportal.ManagementPortalClient
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.converter.avro.AvroConverter
import org.radarbase.iot.sender.KafkaAvroDataSender
import org.slf4j.LoggerFactory
import java.io.IOException

open class RestProxyDataConsumer : DataConsumer<AvroConverter<*, *>> {
    private val kafkaDataSender: KafkaAvroDataSender

    constructor(
        uploadIntervalSeconds: Int,
        maxCacheSize: Int,
        kafkaDataSender: KafkaAvroDataSender
    ) : super(uploadIntervalSeconds, maxCacheSize) {
        this.kafkaDataSender = kafkaDataSender
    }

    constructor(
        uploadIntervalSeconds: Int,
        maxCacheSize: Int
    ) : super(uploadIntervalSeconds, maxCacheSize) {
        val managementPortalPath = ""

        val managementPortalClient = ManagementPortalClient(
            clientId = CONFIGURATION.radarConfig.oAuthClientId,
            clientSecret = CONFIGURATION.radarConfig.oAuthClientSecret,
            managementPortal = ServerConfig(
                "${CONFIGURATION.radarConfig
                    .baseUrl}/${managementPortalPath}"
            )
        )

        val nitriteProperties =
            CONFIGURATION.persistenceStoreproperties ?: PersistentOAuthStateStore.NitriteProperties(
                filePath = "/usr/local/radar/iot/oauthStore",
                username = null,
                password = null
            )

        val metaTokenUrl = "${CONFIGURATION.radarConfig
            .baseUrl}/${managementPortalPath}/api/meta-token/${CONFIGURATION.radarConfig.metaToken
            .orEmpty()}"

        logger.info("Meta token URL is: $metaTokenUrl")

        this.kafkaDataSender = KafkaAvroDataSender(
            authorizer = ManagementPortalAuthorizer(
                userId = CONFIGURATION.radarConfig.userId,
                managementPortalClient = managementPortalClient,
                loginStrategy = MetaTokenLoginStrategy(
                    metaTokenUrl,
                    managementPortalClient
                ),
                oAuthStateStore = PersistentOAuthStateStore(nitriteProperties),
                sourceId = CONFIGURATION.radarConfig.sourceId,
                // TODO: Change these to the correct ones
                sourceTypeModel = "E4",
                sourceTypeProducer = "Empatica",
                sourceTypeCatalogVersion = "v1"
            ),
            schemaUrl = CONFIGURATION.radarConfig.schemaRegistryUrl,
            kafkaUrl = CONFIGURATION.radarConfig.kafkaUrl
        )
    }

    override fun processData(messages: Map<AvroConverter<*, *>, List<String>>) {
        for ((k, v) in messages) {
            logger.debug("Converting and sending $v using $k")
            try {
                sendToRestProxy(k.convert(v))
            } catch (exc: IOException) {
                logger.warn(
                    "Messages for $k could not be sent. Adding to cache " +
                            "to be sent later...", exc
                )
                GlobalScope.launch(exceptionHadler) {
                    messages.forEach { (t, u) ->
                        u.forEach { handleData(it, t) }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    open fun <K, V> sendToRestProxy(records: RecordData<K, V>) {
        try {
            kafkaDataSender.sendAll(records)
        } catch (exc: SchemaValidationException) {
            logger.error(
                "Messages for ${records.topic} could not be sent due to schema " +
                        "validation failure. Discarding these messages.", exc
            )
        }
        logger.info("Successfully uploaded ${records.size()} records.")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)

        private val exceptionHadler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
            logger.warn("Error while uploading records to Rest proxy", e)
        }
    }
}
