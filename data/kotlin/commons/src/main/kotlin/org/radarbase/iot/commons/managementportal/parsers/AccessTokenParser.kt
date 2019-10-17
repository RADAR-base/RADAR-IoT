package org.radarbase.iot.commons.managementportal.parsers

import org.radarbase.iot.commons.auth.OAuthState
import okhttp3.Headers
import org.json.JSONException
import org.json.JSONObject
import org.radarbase.iot.commons.util.Parser
import java.io.IOException
import java.time.Duration
import java.time.Instant

class AccessTokenParser(val oAuthState: OAuthState) : Parser<String, OAuthState> {
    override fun parse(value: String): OAuthState {
        var refreshToken = oAuthState.refreshToken
        try {
            val json = JSONObject(value)
            val accessToken = json.getString("access_token")
            refreshToken = json.optString("refresh_token", refreshToken)

            return oAuthState.apply {
                this.refreshToken = refreshToken
                this.accessToken = accessToken
                this.expiration =
                    Instant.now() + Duration.ofSeconds(json.optLong("expires_in", 3600L))
                this.httpHeaders =
                    Headers.Builder().add("Authorization", "Bearer $accessToken").build()
            }
        } catch (ex: JSONException) {
            throw IOException("Failed to parse json string $value", ex)
        }
    }

}
