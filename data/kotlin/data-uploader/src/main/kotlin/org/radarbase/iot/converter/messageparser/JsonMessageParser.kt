package org.radarbase.iot.converter.messageparser

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.radarbase.iot.commons.util.Parser
import java.io.IOException

/**
 * Message [Parser] for parsing messages read from the pub/sub system in json format.
 * Another parser (for example if messages in the pub/sub system are in Avro format) can be
 * created similarly by implementing the [Parser] interface.
 * [typeReference] to be supplied for type-safe conversions.
 */
class JsonMessageParser<out T>(private val typeReference: TypeReference<T>) : Parser<String, T> {

    @Throws(IOException::class)
    override fun parse(value: String): T {
        return objectMapper.readValue(value, typeReference)
    }

    companion object {
        private val objectMapper = ObjectMapper().also { it.registerModule(KotlinModule.Builder().build()) }
    }
}