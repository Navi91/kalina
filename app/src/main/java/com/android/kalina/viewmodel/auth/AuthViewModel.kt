package com.android.kalina.viewmodel.auth

import android.arch.lifecycle.MutableLiveData
import com.android.kalina.api.ApiException
import com.android.kalina.api.auth.AuthApi
import com.android.kalina.api.auth.AuthHolder
import com.android.kalina.api.auth.WrongCodeApiException
import com.android.kalina.api.util.RxHelper
import com.android.kalina.dagger.ComponentHolder
import com.android.kalina.data.response.ApiResponse
import com.android.kalina.data.response.ConfirmCodeResponse
import com.android.kalina.data.response.ConfirmPhoneResponse
import com.android.kalina.viewmodel.Studio21ViewModel
import retrofit2.HttpException
import javax.inject.Inject

class AuthViewModel : Studio21ViewModel() {

    @Inject
    lateinit var authHolder: AuthHolder
    @Inject
    lateinit var authApi: AuthApi

    private val authModel = MutableLiveData<AuthModel>()

    private val authStateLiveData = MutableLiveData<AuthState>()
    private val progressLiveData = MutableLiveData<Boolean>()
    private val errorLiveData = MutableLiveData<Throwable?>()

    init {
        ComponentHolder.applicationComponent().inject(this)

        authModel.value = AuthModel()
        authStateLiveData.value = AuthState.REQUEST_AUTH_CREDENTIAL
        progressLiveData.value = false
        errorLiveData.value = null
    }

    fun getAuthModel() = authModel

    fun getAuthStateData() = authStateLiveData

    fun getProgressData() = progressLiveData

    fun getErrorData() = errorLiveData

    fun requestCode() {
        setLoading(true)

        authModel.value?.let {
            RxHelper.makeBackground(authApi.requestCode(it.phone))
                    .doOnTerminate { setLoading(false) }
                    .subscribe(object : ApiObserver<ConfirmPhoneResponse>() {
                        override fun onNext(t: ConfirmPhoneResponse) {
                            if (t.status == ApiResponse.STATUS_OK) {
                                setState(AuthState.REQUEST_AUTH_CODE)
                            } else {
                                setError(ApiException(t.message))
                            }
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)

                            setError(e)
                        }
                    })
        }
    }

    fun confirmCode() {
        setLoading(true)

        authModel.value?.let {
            RxHelper.makeBackground(authApi.confirmCode(it.code, it.name, it.phone))
                    .doOnTerminate { setLoading(false) }
                    .subscribe(object : ApiObserver<ConfirmCodeResponse>() {
                        override fun onNext(t: ConfirmCodeResponse) {
                            if (t.status == ApiResponse.STATUS_OK) {
                                saveAuthData(t.deviceCode)
                                setState(AuthState.AUTH_DONE)
                            } else {
                                setError(ApiException(t.message))
                            }
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)

                            if (e is HttpException && e.code() == 400) {
                                setError(WrongCodeApiException())
                            } else {
                                setError(e)
                            }
                        }
                    })
        }
    }

    fun setName(name: String) {
        authModel.value = authModel.value?.setName(name)
    }

    fun setPhone(phone: String) {
        authModel.value = authModel.value?.setPhone(phone)
    }

    fun setCode(code: String) {
        authModel.value = authModel.value?.setCode(code)
    }

    private fun setLoading(loading: Boolean) {
        progressLiveData.value = loading
    }

    private fun setError(error: Throwable?) {
        errorLiveData.value = error
    }

    private fun setState(state: AuthState) {
        authStateLiveData.value = state
    }

    private fun saveAuthData(deviceCode: String) {
        val value = authModel.value ?: throw IllegalStateException()

        authHolder.setPhone(value.phone)
        authHolder.setName(value.name)
        authHolder.setDeviceCode(deviceCode)
    }
}