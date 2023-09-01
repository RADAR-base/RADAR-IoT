package org.radarbase.iot

import org.radarbase.iot.config.Configuration.Companion.CONFIGURATION

fun main(args: Array<String>) {

    Runtime.getRuntime().addShutdownHook(Thread {
        CONFIGURATION.internalBroker.stop()
    })

    CONFIGURATION.internalBroker.apply {
        initialise()
        start()
    }
}