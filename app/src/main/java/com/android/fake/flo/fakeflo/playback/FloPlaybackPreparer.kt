@file:Suppress("DEPRECATION")

package com.android.fake.flo.fakeflo.playback

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector


class FloPlaybackPreparer(private val player: FloPlayer) : MediaSessionConnector.PlaybackPreparer {
    companion object {
        private const val TAG = "FloPlaybackPreparer"
    }

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_URI or
            PlaybackStateCompat.ACTION_PLAY_FROM_URI
    }

    override fun onPrepare(playWhenReady: Boolean) = Unit

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) = Unit
    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
        Log.d(TAG, "onPrepareFromUri uri : $uri, playWhenReady : $playWhenReady")
        player.setMediaSource(uri)
        if (playWhenReady) {
            player.start()
        }
    }

    override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle?, cb: ResultReceiver?) = false
}