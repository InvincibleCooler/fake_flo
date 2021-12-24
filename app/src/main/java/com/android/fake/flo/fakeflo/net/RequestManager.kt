package com.android.fake.flo.fakeflo.net

import okhttp3.OkHttpClient


class RequestManager {
    companion object {
        private const val TAG = "RequestManager"

        fun getServiceApi(headers: HashMap<String, String>?): ServiceApi {
            val okHttpClient: OkHttpClient = RequestClient(headers).client
            val retrofit = RetrofitClient.getInstance(okHttpClient)
            return retrofit.create(ServiceApi::class.java)
        }
    }
}