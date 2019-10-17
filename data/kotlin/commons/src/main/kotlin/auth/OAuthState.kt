package auth

import okhttp3.Headers
import java.time.Instant

data class OAuthState(
    var refreshToken: String,
    var accessToken: String,
    var httpHeaders: Headers?,
    var expiration: Instant
)