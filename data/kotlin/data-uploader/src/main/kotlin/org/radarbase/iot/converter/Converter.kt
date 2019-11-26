package org.radarbase.iot.converter

interface Converter<in S, out T> {
    fun convert(messages: S): T

}