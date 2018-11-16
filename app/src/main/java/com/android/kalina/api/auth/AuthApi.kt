package com.android.kalina.api.auth
//package com.android.kalina.api.auth

import com.android.kalina.api.retrofit.RetrofitApi
import com.android.kalina.data.response.ConfirmCodeResponse
import com.android.kalina.data.response.ConfirmPhoneResponse
import io.reactivex.Observable
import javax.inject.Inject

class AuthApi @Inject constructor(private val retrofitApi: RetrofitApi) {

    fun requestCode(phone: String): Observable<ConfirmPhoneResponse> {
        return retrofitApi.doGetConfirmPhoneAuth(phone)
    }

    fun confirmCode(code: String, name: String, phone: String): Observable<ConfirmCodeResponse> {
        return retrofitApi.doGetConfirmCodeAuth(code, name, phone)
    }
}