package org.radarbase.iot.commons.auth

import okhttp3.Headers
import org.radarbase.iot.commons.managementportal.ManagementPortalClient
import org.radarbase.iot.commons.managementportal.parsers.AccessTokenParser
import org.radarbase.iot.commons.managementportal.parsers.SubjectParser
import org.slf4j.LoggerFactory
import java.time.Instant

class ManagementPortalAuthorizer(
    private val userId: String,
    private val managementPortalClient: ManagementPortalClient,
    private val oAuthStateStore: OAuthStateStore,
    private val loginStrategy: LoginStrategy<*>,
    private val sourceId: String,
    private val sourceTypeModel: String = DEFAULT_SOURCE_TYPE_MODEL,
    private val sourceTypeProducer: String = DEFAULT_SOURCE_TYPE_PRODUCER,
    private val sourceTypeCatalogVersion: String = DEFAULT_SOURCE_TYPE_CATALOG_VERSION
) :
    Authorizer {

    private lateinit var oAuthState: OAuthState
    private var isLoggedIn = false

    @Synchronized
    override fun login() {
        var oAuthState1 = oAuthStateStore.getOAuthState(null)

        if (oAuthState1 == null) {
            oAuthState = getRefreshToken()
            oAuthStateStore.saveOAuthState(null, oAuthState)
            isLoggedIn = true
        } else {
            if (oAuthState1.refreshToken.isEmpty()) {
                logger.warn("The refresh token was not found. Trying to login again.")
                oAuthState1 = getRefreshToken()
                oAuthStateStore.saveOAuthState(null, oAuthState1)
            }
            oAuthState = oAuthState1.also { isLoggedIn = true }
        }
    }

    override fun initialise() {
        if (!isLoggedIn) {
            login()
        }

        val subject = managementPortalClient.getSubject(userId, getAuthHeader(), SubjectParser())
        check(subject.sourceTypes.any {
            it.model == sourceTypeModel
                    && it.producer == sourceTypeProducer
                    && it.catalogVersion == sourceTypeCatalogVersion
        }) {
            // TODO: Register source if not found.
            "The Source type is not registered for the participant. " +
                    "Please register it manually first."
        }

        check(subject.sourcesMetadata.any { it.sourceId == sourceId }) {
            "The provided Source Id $sourceId does not match with the sources associated with " +
                    "the user $userId"
        }
    }

    /**
     * Get the valid access token. If access token has expired, will try to get a new Access
     * token from the Management portal.
     * Throws [IllegalStateException] if access token is `null` or if not logged in.
     */
    override fun getAccessToken() = getOAuthState().accessToken

    override fun getOAuthState(): OAuthState {
        check(isLoggedIn) { "Please login first" }

        return if (!isExpired()) {
            oAuthState
        } else {
            oAuthState =
                managementPortalClient.refreshToken(oAuthState, AccessTokenParser(oAuthState))
            oAuthStateStore.saveOAuthState(null, oAuthState)
            return oAuthState
        }
    }


    override fun getAuthHeader(): Headers {
        val oAuthState1 = getOAuthState()
        return oAuthState1.httpHeaders
            ?: Headers.Builder()
                .set("Authorization", "Bearer ${oAuthState1.accessToken}")
                .build()
    }

    override fun isLoggedIn() = isLoggedIn

    override fun isExpired(): Boolean = oAuthState.expiration.isBefore(Instant.now())

    private fun getRefreshToken(): OAuthState {
        val refreshToken = loginStrategy.getRefreshToken()
            ?: throw IllegalStateException(
                "Cannot login using the authoriser provided." +
                        "The refresh token was not available."
            )

        val oAuthStateTemp = OAuthState(
            refreshToken = refreshToken,
            accessToken = "",
            httpHeaders = null,
            expiration = Instant.now()
        )

        // Save the temp oauth state (contains refresh token)
        oAuthStateStore.saveOAuthState(null, oAuthStateTemp)

        // get access token and new refresh token
        return managementPortalClient.refreshToken(
            oAuthStateTemp,
            AccessTokenParser(oAuthStateTemp)
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)

        var DEFAULT_SOURCE_TYPE_MODEL = "RADAR-IoT"
        var DEFAULT_SOURCE_TYPE_PRODUCER = "RADAR"
        var DEFAULT_SOURCE_TYPE_CATALOG_VERSION = "1.0.0"
    }
}