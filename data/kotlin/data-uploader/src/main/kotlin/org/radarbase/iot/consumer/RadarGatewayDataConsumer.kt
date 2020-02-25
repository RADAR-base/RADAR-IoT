package org.radarbase.iot.consumer

import org.radarbase.config.ServerConfig
import org.radarbase.iot.commons.auth.ManagementPortalAuthorizer
import org.radarbase.iot.commons.auth.MetaTokenLoginStrategy
import org.radarbase.iot.commons.auth.PersistentOAuthStateStore
import org.radarbase.iot.commons.managementportal.ManagementPortalClient
import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION
import org.radarbase.iot.sender.KafkaAvroDataSender
import org.slf4j.LoggerFactory

open class RadarGatewayDataConsumer : RestProxyDataConsumer {
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
            clientId = checkNotNull(CONFIGURATION.radarConfig.oAuthClientId),
            clientSecret = checkNotNull(CONFIGURATION.radarConfig.oAuthClientSecret),
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
            .baseUrl}/${managementPortalPath}/api/meta-token/${checkNotNull(
            CONFIGURATION.radarConfig
                .metaToken
        )}"

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
                sourceTypeModel = CONFIGURATION.radarConfig.sourceTypeModel,
                sourceTypeProducer = CONFIGURATION.radarConfig.sourceTypeProducer,
                sourceTypeCatalogVersion = CONFIGURATION.radarConfig.sourceTypeCatalogVersion
            ),
            schemaUrl = CONFIGURATION.radarConfig.schemaRegistryUrl,
            kafkaUrl = CONFIGURATION.radarConfig.kafkaUrl
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
