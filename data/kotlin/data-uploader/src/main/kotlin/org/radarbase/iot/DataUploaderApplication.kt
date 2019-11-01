package org.radarbase.iot

import org.radarbase.iot.handler.DataHandler
import org.radarbase.iot.handler.Handler

fun main(args: Array<String>) {
    val dataHandlers: Array<Handler> = arrayOf(DataHandler())
    dataHandlers.forEach {
        it.apply {
            initialise()
            start()
        }
    }
}