package managementportal.parsers

import auth.MetaToken
import org.json.JSONException
import org.json.JSONObject
import util.Parser
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