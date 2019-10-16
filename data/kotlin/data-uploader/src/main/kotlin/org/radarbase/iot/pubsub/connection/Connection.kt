package org.radarbase.iot.pubsub.connection

interface Connection {

    fun getConnection(): Any

    fun getConnectionPool(): Any
}