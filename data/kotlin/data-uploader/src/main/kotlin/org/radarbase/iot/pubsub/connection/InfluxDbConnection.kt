package org.radarbase.iot.pubsub.connection

import org.influxdb.*
import org.influxdb.dto.Query
import org.radarbase.iot.config.Configuration
import org.slf4j.LoggerFactory

class InfluxDbConnection(private val influxDbConfig: Configuration.InfluxDbConfig) :
    Connection<InfluxDB> {

    init {
        InfluxDBFactory.connect(
            influxDbConfig.url, influxDbConfig.username, influxDbConfig.password
        ).apply {
            query(Query("CREATE DATABASE " + influxDbConfig.dbName))

            query(
                Query(
                    """CREATE RETENTION POLICY ${influxDbConfig.retentionPolicyName} ON 
${influxDbConfig.dbName} DURATION ${influxDbConfig.retentionPolicyDuration} 
REPLICATION ${influxDbConfig.retentionPolicyReplicationFactor} DEFAULT"""
                )
            )
        }
    }

    @Throws(InfluxDBIOException::class)
    override fun getConnection(): InfluxDB = createConnection()

    override fun getConnectionPool(): Any {
        TODO("not implemented in the underlying influxdb client")
    }

    override fun isConnected(): Boolean = try {
        createConnection().ping().isGood
    } catch (exc: InfluxDBException) {
        false
    }

    private fun createConnection(): InfluxDB = InfluxDBFactory.connect(
        influxDbConfig.url, influxDbConfig.username, influxDbConfig.password
    ).apply {
        setDatabase(influxDbConfig.dbName)
        setRetentionPolicy(influxDbConfig.retentionPolicyName)

        enableBatch(BatchOptions.DEFAULTS.exceptionHandler { t, u ->
            logger.warn(
                "Error thrown when writing data to influxDB.", u
            )
        })
        enableGzip()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InfluxDbConnection::class.java)
    }
}