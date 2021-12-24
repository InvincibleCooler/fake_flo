package com.android.fake.flo.fakeflo.playback

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class FloPlayer(private val context: Context) {
    companion object {
        private const val TAG = "FloPlayer"
    }

    interface Callback {
        fun onCompletion()
        fun onPlaybackStateChanged(playWhenReady: Boolean, state: Int)
        fun onError(error: String)
    }

    var callback: Callback? = null

    private val simpleExoPlayer: SimpleExoPlayer by lazy {
        /**
         * default track selector is AdaptiveTrackSelection :
         * A bandwidth based adaptive TrackSelection, whose selected track is updated to be the one of highest quality given the current network conditions and the state of the buffer.
         */
        SimpleExoPlayer.Builder(context).build().apply {
            addListener(eventListener)
        }
    }

    private val eventListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            val errorMsg = error.errorCodeName
            Log.e(TAG, "onPlayerError error msg : $errorMsg")
            callback?.onError(errorMsg)
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_READY -> {
                    callback?.onPlaybackStateChanged(playWhenReady, playbackState)
                }
                Player.STATE_ENDED -> {
                    callback?.onCompletion()
                }
            }
        }
    }

    fun getPlayer(): SimpleExoPlayer {
        return simpleExoPlayer
    }

    fun isPlaying(): Boolean {
        return simpleExoPlayer.playWhenReady
    }

    // Util stuff function
    fun getCurrentPosition(): Long {
        return simpleExoPlayer.currentPosition
    }

    fun getDuration(): Long {
        return simpleExoPlayer.duration
    }

    fun seekTo(positionMs: Long) {
        simpleExoPlayer.seekTo(positionMs)
    }

    fun setVolume(audioVolume: Float) {
        simpleExoPlayer.volume = audioVolume
    }

    fun next() {
        if (simpleExoPlayer.hasNext()) {
            simpleExoPlayer.next()
        }
    }

    fun previous() {
        if (simpleExoPlayer.hasPrevious()) {
            simpleExoPlayer.previous()
        }
    }

    fun start() {
        simpleExoPlayer.playWhenReady = true
    }

    fun pause() {
        simpleExoPlayer.playWhenReady = false
    }

    fun stop(reset: Boolean) {
        simpleExoPlayer.stop(reset)
    }

    fun release() {
        simpleExoPlayer.release()
        simpleExoPlayer.removeListener(eventListener)
        // should be null outside of the player
    }

    fun setMediaSource(uri: Uri) {
        val mediaSource = buildMediaSource(uri)
        if (mediaSource != null) {
            simpleExoPlayer.setMediaSource(mediaSource)
            simpleExoPlayer.prepare()
        } else {
            Log.e(TAG, "Media source is not available.")
        }
    }

    /**
     * Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ONE, Player.REPEAT_MODE_ALL
     */
    fun updateRepeatMode(repeatMode: Int) {
        simpleExoPlayer.repeatMode = repeatMode
    }

    private fun buildMediaSource(uri: Uri): MediaSource? {
        return when (Util.inferContentType(uri)) {
            C.TYPE_HLS -> {
                Log.d(TAG, "TYPE_HLS")
                HlsMediaSource.Factory(buildDataSourceFactory()).createMediaSource(uri)
            }
            C.TYPE_OTHER -> {
                Log.d(TAG, "TYPE_OTHER")
                ProgressiveMediaSource.Factory(buildDataSourceFactory()).createMediaSource(uri)
            }
            else -> {
                Log.d(TAG, "DASH, SS are not supported.")
                null
            }
        }
    }

    private fun buildDataSourceFactory() =
        DefaultDataSourceFactory(context, Util.getUserAgent(context, "FakeFlo"))

    // for debug
    fun stateName(state: Int): String {
        return when (state) {
            Player.STATE_IDLE -> {
                "STATE_IDLE"
            }
            Player.STATE_BUFFERING -> {
                "STATE_BUFFERING"
            }
            Player.STATE_READY -> {
                "STATE_READY"
            }
            Player.STATE_ENDED -> {
                "STATE_ENDED"
            }
            else -> {
                "illegal state"
            }
        }
    }
}