package com.android.fake.flo.fakeflo.noti

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.android.fake.flo.fakeflo.MainActivity
import com.android.fake.flo.fakeflo.R
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.*

const val NOTIFICATION_CHANNEL_ID = "com.android.fake.flo.fakeflo.NOW_PLAYING"
const val NOTIFICATION_ID = 10000

class FloNotificationManager(private val context: Context, sessionToken: MediaSessionCompat.Token, notificationListener: PlayerNotificationManager.NotificationListener) {
    companion object {
        private const val TAG = "FloNotificationManager"
        private const val NOTIFICATION_LARGE_ICON_SIZE = 144 // px
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)
        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID,
        ).apply {
            setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            setNotificationListener(notificationListener)
            setChannelNameResourceId(R.string.notification_channel_name)
            setChannelDescriptionResourceId(R.string.notification_channel_description)
            setSmallIconResourceId(R.mipmap.ic_launcher)
        }.build().apply {
            setMediaSessionToken(sessionToken)
//            setUseNextAction(true) // 내부 코드를 보니 노래가 한곡이라서 안보이는 듯.
//            setUsePreviousAction(false)
//            setUseFastForwardAction(false)
//            setUseRewindAction(false)
            setUseStopAction(true)
        }
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    fun showNotificationForPlayer(player: Player?) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        private var currentIconUri: String? = null
        private var currentBitmap: Bitmap? = null

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val launchActivityIntent = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(context, 0, launchActivityIntent, 0)
        }

        override fun getCurrentContentTitle(player: Player): String {
            val title = controller.metadata.description.title.toString()
            if (title.isEmpty()) {
                return ""
            }
            return title
        }

        override fun getCurrentContentText(player: Player): String {
            val subTitle = controller.metadata.description.subtitle.toString()
            if (subTitle.isEmpty()) {
                return ""
            }
            return subTitle
        }

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            val iconUri = controller.metadata.description.iconUri.toString()
            return if (currentIconUri != iconUri || currentBitmap == null) {
                // Cache the bitmap for the current song so that successive calls to
                // `getCurrentLargeIcon` don't cause the bitmap to be recreated.
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = getBitmap(iconUri)
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun getBitmap(albumArtUri: String): Bitmap? {
            return withContext(Dispatchers.IO) {
                try {
                    Glide.with(context).asBitmap().load(albumArtUri).submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE).get()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}