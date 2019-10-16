package org.radarbase.iot.auth

import okhttp3.Headers

interface Authorizer {

    fun login()

    fun initialise()

    fun getAccessToken()

    fun getAuthHeader(): Headers
}