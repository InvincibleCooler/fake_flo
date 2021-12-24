package com.android.fake.flo.fakeflo.playback

import android.util.Log
import com.android.fake.flo.fakeflo.viewmodel.Playable


object PlaylistManager {
    private const val TAG = "PlaylistManager"

    var songList: ArrayList<Playable> = ArrayList()
    private var currentIndex = 0


    fun findPlayable(filePath: String): Playable? {
        val playables = songList.filter {
            filePath == it.file
        }
        return playables.firstOrNull()
    }

    fun setCurrentIndex(index: Int) {
        currentIndex = index
    }

    fun getCurrentIndex(): Int {
        return currentIndex
    }

    fun clear() {
        songList.clear()
        currentIndex = 0
    }

    fun getCurrentPlayable(reason: String? = null): Playable? {
        Log.i(TAG, "getCurrentPlayable() - reason : $reason")
        if (songList.isEmpty()) {
            Log.i(TAG, "getCurrentPlayable() - songList is empty!!!")
            return null
        }

        val size = songList.size

        return if (currentIndex < size) {
            songList[currentIndex]
        } else {
            null
        }
    }
}