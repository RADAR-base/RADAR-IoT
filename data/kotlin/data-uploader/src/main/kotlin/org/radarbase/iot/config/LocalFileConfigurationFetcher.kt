package org.radarbase.iot.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.radarbase.iot.commons.exception.ConfigurationException
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class LocalFileConfigurationFetcher : ConfigurationFetcher {
    private var fileLastModified: Long? = null

    override fun fetchConfig(): Configuration = loadPropertiesFromFile()

    /**
     * Checks if the file has been modified on disk
     */
    override fun hasUpdates(): Boolean {
        val configFile = File(configFilePath)
        if (!configFile.exists()) {
            throw ConfigurationException("Error loading config file.")
        }

        return if (fileLastModified != null) {
            fileLastModified == configFile.lastModified()
        } else {
            fileLastModified = configFile.lastModified()
            true
        }
    }

    @Throws(ConfigurationException::class)
    internal fun loadPropertiesFromFile(): Configuration {
        val inputStream: InputStream
        try {
            inputStream = FileInputStream(File(configFilePath))
        } catch (e: Exception) {
            logger.warn(
                "Could not load configuration from the File Path: ${configFilePath}." +
                        " Trying to load from the Classpath..."
            )
            throw ConfigurationException("Could not load Configuration.")
        }

        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(KotlinModule.Builder().build()) // Enable Kotlin support
        return inputStream.use {
            mapper.readValue(it, Configuration::class.java)
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)

        const val ENV_CONFIG_LOCATION_PROPERTY = "RADAR_IOT_CONFIG_LOCATION"
        const val CONFIG_FILE_NAME_DEFAULT = "radar_iot_config.yaml"

        private val configFilePath: String by lazy {
            System.getenv(ENV_CONFIG_LOCATION_PROPERTY) ?: "/radar-iot/${CONFIG_FILE_NAME_DEFAULT}"
        }
    }

}