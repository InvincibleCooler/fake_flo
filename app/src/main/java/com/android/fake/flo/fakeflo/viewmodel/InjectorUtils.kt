package com.android.fake.flo.fakeflo.viewmodel

import android.content.ComponentName
import android.content.Context
import com.android.fake.flo.fakeflo.playback.MusicServiceConnection
import com.android.fake.flo.fakeflo.playback.PlaybackService


object InjectorUtils {
    private fun provideMusicServiceConnection(context: Context): MusicServiceConnection {
        return MusicServiceConnection.getInstance(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
    }

    fun providePlayerViewModel(context: Context): PlayerViewModel.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection = provideMusicServiceConnection(applicationContext)
        return PlayerViewModel.Factory(musicServiceConnection)
    }

    fun provideLyricViewModel(playerViewModel: PlayerViewModel): LyricViewModel.Factory {
        return LyricViewModel.Factory(playerViewModel)
    }
}