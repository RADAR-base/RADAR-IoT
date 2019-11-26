package org.radarbase.iot.pubsub.connection

import java.io.Closeable

interface Connection<T>: Closeable {

    fun getConnection(): T

    fun getConnectionPool(): Any

    fun isConnected(): Boolean
}