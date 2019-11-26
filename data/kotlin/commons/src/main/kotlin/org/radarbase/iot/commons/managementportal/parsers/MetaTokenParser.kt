package org.radarbase.iot.commons.managementportal.parsers

import org.radarbase.iot.commons.auth.MetaToken
import org.json.JSONException
import org.json.JSONObject
import org.radarbase.iot.commons.util.Parser
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