package com.android.kalina.api.auth

import com.android.kalina.api.util.Preferences
import javax.inject.Inject

class AuthHolder @Inject constructor(private val preferences: Preferences) {

    fun setName(name: String) {
        preferences.setName(name)
    }

    fun setPhone(phone: String) {
        preferences.setPhone(phone)
    }

    fun getName(): String {
        return preferences.getName()
    }

    fun getPhone(): String {
        return preferences.getPhone()
    }

    fun setDeviceCode(token: String) {
        preferences.setDeviceCode(token)
    }

    fun getDeviceCode(): String {
        return preferences.getDeviceCode()
    }

    fun isAuth(): Boolean {
        return getDeviceCode().isNotEmpty()
    }
}