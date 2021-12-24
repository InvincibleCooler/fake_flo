package com.android.fake.flo.fakeflo.net

import com.android.fake.flo.fakeflo.net.res.SongRes
import retrofit2.Call
import retrofit2.http.GET


interface ServiceApi {
    // 태그 리스트
    @GET("2020-flo/song.json")
    fun getSongInfo(): Call<SongRes>
}