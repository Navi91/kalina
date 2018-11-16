package com.android.kalina.viewmodel

import android.arch.lifecycle.ViewModel
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class KalinaViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()

        compositeDisposable.dispose()
    }

    private fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    protected abstract inner class ApiObserver<T> : Observer<T> {
        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable) {
            addDisposable(d)
        }

        abstract override fun onNext(t: T)

        override fun onError(e: Throwable) {
        }

    }
}