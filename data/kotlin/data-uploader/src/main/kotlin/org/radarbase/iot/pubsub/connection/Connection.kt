package org.radarbase.iot.pubsub.connection

interface Connection<T> {

    fun getConnection(): T

    fun getConnectionPool(): Any

    fun isConnected(): Boolean
}