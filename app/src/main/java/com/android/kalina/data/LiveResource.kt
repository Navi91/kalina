package com.android.kalina.data

open class LiveResource<T>(val status: Status, val data: T?, val error: Throwable?) {

    companion object {
        fun <K> success(data: K) = LiveResource(Status.SUCCESS, data, null)

        fun <K> error(error: Throwable, data: K?) = LiveResource(Status.ERROR, data, error)

        fun <K> loading(data: K?) = LiveResource(Status.LOADING, data, null)
    }

}