package org.radarbase.iot.config

interface ConfigurationFetcher {
    fun fetchConfig(): Configuration

    fun hasUpdates(): Boolean

    companion object {
        val ConfigFetcher: ConfigurationFetcher = LocalFileConfigurationFetcher()
    }
}