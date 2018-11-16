package com.android.kalina.audiorecord

import android.content.Context
import com.android.kalina.api.util.Preferences
import com.android.kalina.dagger.ComponentHolder
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Dmitriy on 08.02.2018.
 */
class AudioApi @Inject constructor(val context: Context) {

    private val BASE_URL = "https://dronirovanie.gq/api/"
    private val UPLOAD_FILE_URL = BASE_URL + "upload_file/"

    @Inject
    lateinit var preferences: Preferences

    private val httpClient = OkHttpClient()

    init {
        ComponentHolder.applicationComponent().inject(this)
    }

    fun uploadFile(file: File,
                   successCallback: (deviceCode: String) -> Unit,
                   errorCallback: (error: String?) -> Unit) {
        val url = HttpUrl.parse(UPLOAD_FILE_URL)
                ?.newBuilder()
                ?.addQueryParameter("phone",  preferences.getPhone())
                ?.addQueryParameter("device_code", preferences.getDeviceCode())
                ?.build()

        val name = file.name
        val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", name, RequestBody.create(MediaType.parse("audio/acc"), file))
                .build()

        val request = Request.Builder()
                .url(url)
                .method("POST", body)
                .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                errorCallback(e?.message)
            }

            override fun onResponse(call: Call?, response: Response?) {
                val json = JSONObject(response?.body()?.string())

                if (response?.code() == 200) {
                    successCallback(json.optString("url"))
                } else {
                    errorCallback(json.optString("message"))
                }
            }
        })
    }
}