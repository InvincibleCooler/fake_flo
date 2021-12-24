package com.android.fake.flo.fakeflo.net

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    // For Singleton instantiation.
    @Volatile
    private var instance: Retrofit? = null

    fun getInstance(okHttpClient: OkHttpClient): Retrofit =
        instance ?: synchronized(this) {
            instance ?: Retrofit.Builder()
                .baseUrl("https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        }
}