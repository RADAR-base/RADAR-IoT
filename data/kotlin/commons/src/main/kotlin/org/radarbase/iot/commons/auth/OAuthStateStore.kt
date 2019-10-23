package org.radarbase.iot.commons.auth

interface OAuthStateStore {

    /**
     * Get [OAuthState] from the state store. returns [null] if the state store was not found.
     * Optional parameter [key] provided to get as this identifier. If not present should get
     * using a fixed default identifier.
     */
    fun getOAuthState(key: String?): OAuthState?

    /**
     * Save [OAuthState] in the state store. Optional parameter [key] provided to store as this
     * identifier. If not present should save using a fixed default identifier.
     */
    fun saveOAuthState(key: String?, oAuthState: OAuthState)
}