package com.android.fake.flo.fakeflo.net

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


class RequestClient(private val headers: HashMap<String, String>?) {
    companion object {
        private const val TAG = "RequestClient"

        private const val CONNECT_TIME_OUT = 10L
        private const val READ_TIME_OUT = 10L
        private const val WRITE_TIME_OUT = 10L
    }

    var client: OkHttpClient

    init {
        client = OkHttpClient().newBuilder().apply {
            followRedirects(true)
            followSslRedirects(true)
            connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
            readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            addNetworkInterceptor(AddHeaderInterceptor())
        }.build()
    }

    private inner class AddHeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val builder = chain.request().newBuilder()
            if (headers != null && headers.isNotEmpty()) {
                for (key in headers.keys) {
                    val value = headers[key]
                    Log.d(TAG, "$key : $value")
                    if (value != null) {
                        builder.header(key, value)
                    }
                }
            }
            return chain.proceed(builder.build())
        }
    }
}