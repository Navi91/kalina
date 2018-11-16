package com.android.kalina.dagger.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.android.kalina.BuildConfig
import com.android.kalina.api.retrofit.RetrofitApi
import com.android.kalina.api.retrofit.RetrofitCreator
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class RetrofitModule {

    @Provides
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    fun provideRetrofitCreator(okHttpClient: OkHttpClient, gson: Gson) = RetrofitCreator(okHttpClient, gson)

    @Provides
    fun provideRetrofit(creator: RetrofitCreator) = creator.create()

    @Provides
    fun provideRetrofitApi(retrofit: Retrofit): RetrofitApi = retrofit.create(RetrofitApi::class.java)

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        return logger
    }

    @Provides
    @Singleton
    fun provideHeaderInterceptor() = HeaderInterceptor()

    @Provides
    @Singleton
    fun provideOkHttpClient(headerInterceptor: HeaderInterceptor, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS) // connect timeout
                .readTimeout(15, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    class HeaderInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()

            // Request customization: add request headers
            val requestBuilder = original.newBuilder()

//            if (authHolder.isAuth()) {
//                requestBuilder.addHeader("Authorization", authHolder.getDeviceCode())
//            }
//            if (!UserUtils.getTokenSite().isEmpty()) {
//                requestBuilder.addHeader("Site-Access-Token", UserUtils.getTokenSite())
//            }
//            requestBuilder.addHeader("Mobile-Type", "android")
//            requestBuilder.addHeader("Mobile-Version", "2.0.2")
//
//            requestBuilder.addHeader("Connection", "close")
            val request = requestBuilder.build()
            return chain.proceed(request)
        }
    }
}