package org.radarbase.iot

import org.radarbase.iot.handler.RedisDataHandler
import org.radarbase.iot.handler.Handler

fun main(args: Array<String>) {
    val dataHandlers: Array<Handler> = arrayOf(RedisDataHandler())
    dataHandlers.forEach {
        it.apply {
            initialise()
            start()
        }
    }
}