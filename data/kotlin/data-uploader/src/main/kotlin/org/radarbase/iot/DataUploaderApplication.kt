package org.radarbase.iot

import org.radarbase.iot.handler.Handler
import org.radarbase.iot.handler.RedisDataHandler

val dataHandlers: Array<Handler> = arrayOf(RedisDataHandler())

fun main(args: Array<String>) {

    Runtime.getRuntime().addShutdownHook(Thread {
        dataHandlers.forEach { it.stop() }
    })

    dataHandlers.forEach {
        it.apply {
            initialise()
            start()
        }
    }
}