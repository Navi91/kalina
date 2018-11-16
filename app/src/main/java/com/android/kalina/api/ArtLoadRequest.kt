package com.android.kalina.api

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

/**
 * Created by Dmitriy on 25.02.2018.
 */
class ArtLoadRequest(val context: Context, val okHttpClient: OkHttpClient, val author: String, val song: String, val callback: LoadCallback) {

    private val baseUrl = "https://itunes.apple.com/search"

    private var isSubscribed = false;

    companion object {
        val imageHolderMap = mutableMapOf<String, Bitmap>()
    }

    fun subscribe() {
        isSubscribed = true

        load()
    }

    fun unsubscribe() {
        isSubscribed = false
    }

    fun load() {
        if (!isSubscribed) return

        if (imageHolderMap.containsKey(createTermWithAuthorAndSong())) {
            notifyCallback(imageHolderMap.get(createTermWithAuthorAndSong()))
            return
        }

        loadWithTerm(createTermWithAuthorAndSong(), {
            loadWithTerm(createTermWithAuthor(), { notifyCallback(null) })
        })
    }

    private fun loadWithTerm(term: String, failCallback: () -> Unit) {
        if (!isSubscribed) return

        val url = HttpUrl.parse(baseUrl)
                ?.newBuilder()
                ?.addQueryParameter("term", term)
                ?.addQueryParameter("limit", "1")
                ?.build()

        val request = Request.Builder().url(url).build()

        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                notifyCallback(null)
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (!isSubscribed) return

                val imageUrl = getImageUrlFromResponse(response)

                if (imageUrl.isEmpty()) {
                    failCallback()
                } else {
                    loadImage(imageUrl)
                }
            }
        })
    }

    private fun loadImage(url: String) {
        if (!isSubscribed) return

        if (url.isEmpty()) {
            callback.onLoad(null)
            return
        }

        try {
            notifyCallback(Picasso.with(context).load(url).get())
        } catch (e: IOException) {
            e.printStackTrace()
            notifyCallback(null)
        }
    }

    private fun getImageUrlFromResponse(response: Response?): String {
        if (response == null) return ""

        val json = JSONObject(response.body()?.string())

        val count = json.optInt("resultCount")

        if (count == 0) return ""

        val resultArray = json.optJSONArray("results")
        val result = resultArray[0] as JSONObject
        val artworkUrl = result.optString("artworkUrl100")

        if (TextUtils.isEmpty(artworkUrl)) return ""

        return artworkUrl.replace("100x100bb", "600x600bb")
    }

    private fun createTermWithAuthorAndSong() = author + " " + song

    private fun createTermWithAuthor() = author

    private fun notifyCallback(bitmap: Bitmap?) {
        if (bitmap != null) {
            imageHolderMap.put(createTermWithAuthorAndSong(), bitmap)
        }

        if (isSubscribed) {
            callback.onLoad(bitmap)
        }
    }

    interface LoadCallback {
        fun onLoad(bitmap: Bitmap?)
    }
}