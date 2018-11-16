package com.android.kalina.viewmodel.auth


data class AuthModel(val name: String = "",
                     val phone: String = "",
                     val code: String = "") {

    fun setName(name: String) = AuthModel(name, phone, code)

    fun setPhone(phone: String) = AuthModel(name, phone, code)

    fun setCode(code: String) = AuthModel(name, phone, code)

}