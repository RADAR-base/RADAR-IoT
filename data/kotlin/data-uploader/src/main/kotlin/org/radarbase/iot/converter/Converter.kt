package org.radarbase.iot.converter

interface Converter<S, T> {
    fun convert(message: S): T
}