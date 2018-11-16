package com.android.kalina.api

import android.content.Context
import okhttp3.OkHttpClient

/**
 * Created by Dmitriy on 25.02.2018.
 */
class ArtLoader(private val context: Context) {

    private val okHttpClient: OkHttpClient = OkHttpClient()

    fun request(author: String, song: String, callback: ArtLoadRequest.LoadCallback): ArtLoadRequest {
        return ArtLoadRequest(context, okHttpClient, author, song, callback)
    }
}