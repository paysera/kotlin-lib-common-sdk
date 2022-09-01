package com.paysera.lib.common

import com.paysera.lib.common.entities.InRentoApiCredentials
import org.junit.Test
import java.util.*

class ApiCredentialsTest {

    @Test
    fun `XAuthToken - accessTokenExpiresAt - Not Expired`() {
        val accessTokenExpiresAt = Calendar.getInstance().apply {
            add(Calendar.SECOND, 121)
        }.time
        val xAuthApiCredentials = InRentoApiCredentials(
            accessTokenExpiresAt = accessTokenExpiresAt
        )
        assert(!xAuthApiCredentials.hasExpired())
    }

    @Test
    fun `XAuthToken - accessTokenExpiresAt - Expired`() {
        val accessTokenExpiresAt = Calendar.getInstance().apply {
            add(Calendar.SECOND, 60)
        }.time
        val xAuthApiCredentials = InRentoApiCredentials(
            accessTokenExpiresAt = accessTokenExpiresAt
        )
        assert(xAuthApiCredentials.hasExpired())
    }

    @Test
    fun `XAuthToken - accessTokenIssuedAt - Not Expired`() {
        val accessTokenIssuedAt = Calendar.getInstance().apply {
            add(Calendar.SECOND, 16)
        }.time
        val xAuthApiCredentials = InRentoApiCredentials(
            accessTokenIssuedAt = accessTokenIssuedAt
        )
        assert(!xAuthApiCredentials.hasRecentlyRefreshed())
    }

    @Test
    fun `XAuthToken - accessTokenIssuedAt - Expired`() {
        val accessTokenIssuedAt = Calendar.getInstance().apply {
            add(Calendar.SECOND, 10)
        }.time
        val xAuthApiCredentials = InRentoApiCredentials(
            accessTokenIssuedAt = accessTokenIssuedAt
        )
        assert(xAuthApiCredentials.hasRecentlyRefreshed())
    }
}