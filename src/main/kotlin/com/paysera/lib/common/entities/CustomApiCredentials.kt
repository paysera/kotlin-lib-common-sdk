package com.paysera.lib.common.entities

import com.paysera.lib.common.interfaces.BaseApiCredentials

data class CustomApiCredentials(
    val key: String?,
    override var token: String?
) : BaseApiCredentials