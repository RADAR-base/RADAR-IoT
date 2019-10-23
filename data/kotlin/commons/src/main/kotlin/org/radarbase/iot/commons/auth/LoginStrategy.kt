package org.radarbase.iot.commons.auth

interface LoginStrategy<out T> {
    fun getToken(): T?

    fun getRefreshToken(): String?
}