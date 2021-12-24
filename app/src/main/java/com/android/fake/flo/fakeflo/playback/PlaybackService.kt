package com.android.fake.flo.fakeflo.playback

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.android.fake.flo.fakeflo.ext.from
import com.android.fake.flo.fakeflo.noti.FloNotificationManager
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager

/**
 * visit [https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html](https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html).
 * This code is based on the UAMP code "https://github.com/android/uamp"
 */
class PlaybackService : MediaBrowserServiceCompat() {
    companion object {
        private const val TAG = "PlaybackService"
        private const val EMPTY_STRING = ""
    }

    /**
     * 클라이언트가 탐색 없이 MediaSession 에 연결하도록 허용하기 위해서 EMPTY_STRING 를 사용한다.
     * null 을 반환하면 연결이 거부된다.
     */
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
        Log.d(TAG, "onGetRoot")
        return BrowserRoot(EMPTY_STRING, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d(TAG, "onLoadChildren")
        if (EMPTY_STRING == parentId) {
            result.sendResult(null)
            return
        }
    }

    private lateinit var player: FloPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private var floNotificationManager: FloNotificationManager? = null

    private var isForegroundService = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createPlayer()
        createMediaSession()
        createMediaController()
        createMediaSessionConnector()

        floNotificationManager = FloNotificationManager(this, mediaSession.sessionToken, PlayerNotificationListener())
        floNotificationManager?.showNotificationForPlayer(player.getPlayer())
    }

    private fun createPlayer(): FloPlayer {
        player = FloPlayer(this)
        player.callback = object : FloPlayer.Callback {
            override fun onCompletion() {
                Log.d(TAG, "onCompletion()")
            }

            override fun onPlaybackStateChanged(playWhenReady: Boolean, state: Int) {
                Log.d(TAG, "onPlaybackStateChanged() playWhenReady : $playWhenReady, state : $state")
                if (state == PlaybackStateCompat.STATE_PLAYING) {
                    updateMetadata()
                }
            }

            override fun onError(error: String) {
                Log.d(TAG, "onError() error : $error")
            }

        }
        return player
    }

    private fun createMediaSession(): MediaSessionCompat {
        mediaSession = MediaSessionCompat(this, TAG).apply {
            isActive = true
        }
        sessionToken = mediaSession.sessionToken // sessionToken 을 넣어줘야 connect 가 가능함
        return mediaSession
    }

    private fun createMediaController(): MediaControllerCompat {
        mediaController = MediaControllerCompat(this, mediaSession).also {
            it.registerCallback(object : MediaControllerCompat.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                    Log.d(TAG, "createMediaController onMetadataChanged")
                }

                override fun onPlaybackStateChanged(playbackStateCompat: PlaybackStateCompat?) {
                    Log.d(TAG, "createMediaController state : $playbackStateCompat")
                    playbackStateCompat?.let { state ->
//                        updateNotification(state) // 이걸 어떻게 처리해야 할까???
                    }
                }
            })
        }
        return mediaController
    }

    private fun createMediaSessionConnector(): MediaSessionConnector {
        mediaSessionConnector = MediaSessionConnector(mediaSession).also {
            it.setPlayer(player.getPlayer())
            it.setPlaybackPreparer(FloPlaybackPreparer(player))
            it.setQueueNavigator(FloQueueNavigator(mediaController))
        }
        return mediaSessionConnector
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaSession.run {
            isActive = false
            release()
        }
        stopForeground(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            val extras: Bundle = notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE)
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)

            Log.d(TAG, "onNotificationPosted() notificationId: $notificationId, ongoing : $ongoing")
            Log.d(TAG, "onNotificationPosted() title: $title, text : $text")

            if (ongoing && !isForegroundService) {
                Log.i("PlayerNotificationListener", "startForeground()")
                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            Log.d(TAG, "onNotificationCancelled() - dismissedByUser : $dismissedByUser")
            isForegroundService = false
            stopForeground(true)
        }
    }

    private fun updateMetadata() {
        val currentPlayable = PlaylistManager.getCurrentPlayable("$TAG#updateMetadata()")
        if (currentPlayable != null) {
            mediaSession.setMetadata(MediaMetadataCompat.Builder().from(currentPlayable).build())
        }
    }
}