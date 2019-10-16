package org.radarbase.iot.auth

import okhttp3.Headers

interface Authorizer {

    fun login()

    fun initialise()

    fun getAccessToken(): String

    fun getOAuthState(): OAuthState

    fun getAuthHeader(): Headers
}