package org.radarbase.iot.commons.auth

import org.dizitart.kno2.getRepository
import org.dizitart.kno2.nitrite
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.filters.ObjectFilters.eq
import org.radarbase.iot.commons.util.SingletonHolder
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Persistent Store uses Nitrite Database for storing OAuth State. See [OAuthStateStore]
 * The [key] in function parameters should be a number as it is casted to Long
 */
class PersistentOAuthStateStore(val nitriteProperties: NitriteProperties) : OAuthStateStore {

    override fun getOAuthState(key: String?): OAuthState? {
        return dbFactory
            .getInstance(nitriteProperties)
            .getRepository<OAuthStateDoc>().use {
                it.find(eq("id", getId(key))).firstOrDefault()
            }
            ?.oAuthState
    }

    override fun saveOAuthState(key: String?, oAuthState: OAuthState) {
        val idKey = getId(key)
        val result = dbFactory
            .getInstance(nitriteProperties)
            .getRepository<OAuthStateDoc>().use {
                if (it.find(eq("id", idKey)).size() > 0) {
                    it.update(
                        OAuthStateDoc(
                            id = idKey,
                            oAuthState = oAuthState
                        )
                    )
                } else {
                    it.insert(
                        OAuthStateDoc(
                            id = idKey,
                            oAuthState = oAuthState
                        )
                    )
                }
            }
        logger.info("Result of save OAuth State: ${result.affectedCount}")
    }

    data class NitriteProperties(
        val filePath: String,
        val username: String?,
        val password: String?
    )

    data class OAuthStateDoc(
        @Id val id: Long,
        val oAuthState: OAuthState
    )

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private const val DEFAULT_NITRITE_ID = 9999L

        val dbFactory = SingletonHolder<Nitrite, NitriteProperties> {
            if (it.username == null || it.password == null) {
                nitrite {
                    file = File(it.filePath)       // or, path = fileName
                    autoCommitBufferSize = 2048
                    compress = true
                    autoCompact = false
                }
            } else {
                nitrite(it.username, it.password) {
                    file = File(it.filePath)       // or, path = fileName
                    autoCommitBufferSize = 2048
                    compress = true
                    autoCompact = false
                }
            }
        }

        @Throws(NumberFormatException::class)
        private fun getId(optional: String?) = optional?.toLong() ?: DEFAULT_NITRITE_ID
    }
}