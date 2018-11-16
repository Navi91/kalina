package com.android.kalina.api.retrofit

import com.android.kalina.data.response.ConfirmCodeResponse
import com.android.kalina.data.response.ConfirmPhoneResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitApi {

    @GET("confirm/phone/")
    fun doGetConfirmPhoneAuth(@Query("phone") phone: String): Observable<ConfirmPhoneResponse>

    @POST("confirm/phone/code/")
    fun doGetConfirmCodeAuth(@Query("code") code: String, @Query("name") name: String, @Query("phone") phone: String): Observable<ConfirmCodeResponse>
}