package com.android.kalina.api.util

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.io(): Observable<T> {
    return this.subscribeOn(Schedulers.io())
}

fun <T> Observable<T>.observeMain(): Observable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.background(): Observable<T> {
    return this.io().observeMain()
}

fun <T> Single<T>.io(): Single<T> {
    return this.subscribeOn(Schedulers.io())
}

fun <T> Single<T>.observeMain(): Single<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

fun <T> Single<T>.background(): Single<T> {
    return this.io().observeMain()
}

fun Completable.io(): Completable {
    return this.subscribeOn(Schedulers.io())
}

fun Completable.observeMain(): Completable {
    return this.observeOn(AndroidSchedulers.mainThread())
}

fun Completable.background(): Completable {
    return this.io().observeMain()
}

class RxHelper {

    companion object {

        fun <T> makeBackground(observable: Observable<T>): Observable<T> {
            return observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}