package org.radarbase.iot.handler

interface Handler {

    fun initialise()

    fun start()

    fun stop()

    fun isRunning(): Boolean
}