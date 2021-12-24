package com.android.fake.flo.fakeflo.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fake.flo.fakeflo.ext.*
import com.android.fake.flo.fakeflo.playback.MusicServiceConnection


class PlayerViewModel(val musicServiceConnection: MusicServiceConnection) : ViewModel() {
    companion object {
        private const val TAG = "PlayerViewModel"
    }

    fun playMedia(playable: Playable) {
        val transportControls = musicServiceConnection.transportControls

        val nowPlaying = musicServiceConnection.nowPlaying.value
        val playbackState = musicServiceConnection.playbackState.value
        val isPrepared = playbackState?.isPrepared ?: false
        val nowPlayingFilePath = nowPlaying?.filePath
        Log.d(TAG, "isPrepared : $isPrepared, playbackState : ${playbackState?.stateName}")
        Log.d(TAG, "media path : ${playable.file}, nowPlayingFilePath : $nowPlayingFilePath")

        if (isPrepared && playable.file == nowPlayingFilePath) {
            playbackState?.let {
                when {
                    it.isPlaying -> transportControls?.pause()
                    it.isPaused -> transportControls?.play()
                    else -> Log.d(TAG, "Neither play nor pause")
                }
            }
        } else {
            transportControls?.playFromUri(Uri.parse(playable.file), null)
        }
    }

    class Factory(private val musicServiceConnection: MusicServiceConnection) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlayerViewModel(musicServiceConnection) as T
        }
    }
}