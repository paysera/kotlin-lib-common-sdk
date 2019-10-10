package com.paysera.lib.common.exceptions

class ApiErrorProperty(var code: String?, var description: String?) {

    override fun toString(): String {
        return String.format("code=%s, desc=%s", code, description)
    }
}