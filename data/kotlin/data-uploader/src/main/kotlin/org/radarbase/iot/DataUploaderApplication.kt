package org.radarbase.iot

import org.radarbase.iot.handler.DataHandler

fun main(args: Array<String>) {
    val dataHandler = DataHandler()
    dataHandler.apply {
        initialise()
        start()
    }
}