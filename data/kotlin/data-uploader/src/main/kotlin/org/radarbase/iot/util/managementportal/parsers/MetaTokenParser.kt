package org.radarbase.iot.util.managementportal.parsers

import org.json.JSONException
import org.json.JSONObject
import org.radarbase.iot.auth.MetaToken
import org.radarbase.iot.util.Parser
import java.io.IOException

class MetaTokenParser : Parser<String, MetaToken> {
    @Throws(IOException::class)
    override fun parse(value: String): MetaToken {
        try {
            val json = JSONObject(value)
            return MetaToken(
                json.getString("refreshToken"), json.getString("privacyPolicyUrl"),
                json.getString("baseUrl")
            )
        } catch (ex: JSONException) {
            throw IOException("Failed to parse json string $value", ex)
        }
    }
}