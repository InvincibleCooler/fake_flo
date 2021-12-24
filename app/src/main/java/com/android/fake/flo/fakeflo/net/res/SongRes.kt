package com.android.fake.flo.fakeflo.net.res

import com.google.gson.annotations.SerializedName


class SongRes {
    @SerializedName("singer")
    var singer: String? = null
    @SerializedName("album")
    var album: String? = null
    @SerializedName("title")
    var title: String? = null
    @SerializedName("duration")
    var duration: Long = 0L
    @SerializedName("image")
    var image: String? = null
    @SerializedName("file")
    var file: String? = null
    @SerializedName("lyrics")
    var lyrics: String? = null
}