package org.radarbase.iot.commons.auth

import org.radarbase.iot.commons.managementportal.ManagementPortalClient
import org.radarbase.iot.commons.managementportal.parsers.MetaTokenParser

class MetaTokenLoginStrategy(
    val metaTokenUrl: String,
    val managementPortalClient: ManagementPortalClient
) :
    LoginStrategy<MetaToken> {
    override fun getRefreshToken() = getToken().refreshToken

    override fun getToken() =
        managementPortalClient.getRefreshToken(metaTokenUrl, MetaTokenParser())
}