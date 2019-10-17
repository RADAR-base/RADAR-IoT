package auth

import managementportal.ManagementPortalClient
import okhttp3.Headers

class ManagementPortalAuthorizer(val managementPortalClient: ManagementPortalClient) :
    Authorizer {
    override fun login() {
        TODO(
            "Get access and refresh token from storage. If not present try getting it from the " +
                    "meta-token also specified as part of initial configuration"
        ) //To
        // change body of
        // created functions use File |
        // Settings | File
        // Templates.
    }

    override fun initialise() {
        TODO(
            "Check if required sources are present and try to register if not." +
                    "Get Refresh Token and Access token and store them in storage"
        ) //To change body
        // of created functions use File | Settings | File
        // Templates.
    }

    override fun getAccessToken(): String {
        TODO(
            "get Access token from Storage. If not found or is expired then get a new one using " +
                    "the refresh token. If refresh token has also expired then throw unauthorised " +
                    "exception"
        ) //To change body of created functions use File | Settings | File
        // Templates.
    }

    override fun getOAuthState(): OAuthState {
        TODO("Waiting for impl")
    }

    override fun getAuthHeader(): Headers {
        TODO("use getAccessToken() to create and return the Authorization Header") //To change body
        // of created functions use File | Settings |
        // File Templates.
    }
}