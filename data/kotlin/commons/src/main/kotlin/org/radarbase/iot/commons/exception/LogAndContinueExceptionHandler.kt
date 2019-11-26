package org.radarbase.iot.commons.exception

import org.slf4j.LoggerFactory

class LogAndContinueExceptionHandler(val msg: String?) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        logger.error("${msg?.plus("\n") ?: ""}Error thrown in thread $t", e)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}