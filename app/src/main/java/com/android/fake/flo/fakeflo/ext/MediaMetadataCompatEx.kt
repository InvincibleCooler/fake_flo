package com.android.fake.flo.fakeflo.ext

import android.support.v4.media.MediaMetadataCompat
import com.android.fake.flo.fakeflo.viewmodel.Playable

inline val MediaMetadataCompat.filePath: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) ?: ""

inline val MediaMetadataCompat.title: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)

inline val MediaMetadataCompat.artist: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

inline val MediaMetadataCompat.album: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)

inline val MediaMetadataCompat.duration
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

inline val MediaMetadataCompat.lyrics: String?
    get() = getString(METADATA_KEY_LYRICS)

fun MediaMetadataCompat.Builder.from(playable: Playable): MediaMetadataCompat.Builder {
    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, playable.file) // 임시 id값이 없으니 이걸로 구분
    putString(MediaMetadataCompat.METADATA_KEY_TITLE, playable.title)
    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, playable.singer)
    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, playable.album)
    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, playable.duration * 1000)
    putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, playable.image)
    putString(METADATA_KEY_LYRICS, playable.lyrics)
    return this
}

const val METADATA_KEY_LYRICS = "android.media.metadata.LYRICS"