package com.paysera.lib.common.retrofit

abstract class BaseApiClient(
    private val apiRequestManager: ApiRequestManager
) {
    fun cancelCalls() {
        apiRequestManager.cancelCalls()
    }
}