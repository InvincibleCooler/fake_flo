package com.android.fake.flo.fakeflo

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.android.fake.flo.fakeflo.custom.LyricView
import com.android.fake.flo.fakeflo.ext.currentPlayBackPosition
import com.android.fake.flo.fakeflo.ext.stateName
import com.android.fake.flo.fakeflo.net.RequestManager
import com.android.fake.flo.fakeflo.net.res.SongRes
import com.android.fake.flo.fakeflo.playback.PLAYBACK_STATE_NONE
import com.android.fake.flo.fakeflo.playback.PlaylistManager
import com.android.fake.flo.fakeflo.utils.TimeUtil
import com.android.fake.flo.fakeflo.viewmodel.*
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"

        private const val UPDATE_INITIAL_INTERNAL: Long = 0
        private const val UPDATE_INTERNAL: Long = 500
    }

    private val playerViewModel by viewModels<PlayerViewModel> {
        InjectorUtils.providePlayerViewModel(this)
    }

    private val lyricViewModel by viewModels<LyricViewModel> {
        InjectorUtils.provideLyricViewModel(playerViewModel)
    }

    private lateinit var ivThumb: ImageView
    private lateinit var tvSongName: TextView
    private lateinit var tvArtistName: TextView
    private lateinit var tvUpdateTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var ivPlay: ImageView
    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var tvSplash: TextView
    private lateinit var lyricView: LyricView
    private lateinit var tvLyric: TextView

    private var songRes: SongRes? = null

    private var isLoaded = false
    private var lyricDataList = ArrayList<LyricData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivThumb = findViewById(R.id.iv_thumb)
        tvSongName = findViewById(R.id.tv_song_name)
        tvArtistName = findViewById(R.id.tv_artist_name)
        tvUpdateTime = findViewById(R.id.tv_update_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        seekBar = findViewById(R.id.seek_bar)
        ivPlay = findViewById(R.id.iv_play)
        tvSplash = findViewById(R.id.tv_splash)
        lyricView = findViewById(R.id.lyric_view)
        lyricView.setLyricViewModel(lyricViewModel)
        lyricView.listener = object : LyricView.CloseButtonClickListener {
            override fun onCloseButtonClick() {
                expandWindow(false)
            }
        }

        tvLyric = findViewById(R.id.tv_lyric)

        seekBar.run {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                    Log.d(TAG, "onProgressChanged progress : $progress")
                    tvUpdateTime.text = DateUtils.formatElapsedTime(progress.toLong() / 1000)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    stopUpdateSeekBar()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    playerViewModel.musicServiceConnection.transportControls?.seekTo(seekBar.progress.toLong())
                }
            })
        }

        behavior = BottomSheetBehavior.from(lyricView).apply {
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    Log.d(TAG, "newState : $newState")
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
            state = BottomSheetBehavior.STATE_HIDDEN
            skipCollapsed = true
        }

        tvLyric.setOnClickListener {
            expandWindow(true)
        }

        playerViewModel.musicServiceConnection.isConnected.observe(this) {
            Log.d(TAG, "isConnected : $it") // for debugging
            if (it && !songRes?.file.isNullOrEmpty()) {
                playerViewModel.musicServiceConnection.transportControls?.playFromUri(Uri.parse(songRes?.file), null)
            }
        }

        playerViewModel.musicServiceConnection.nowPlaying.observe(this) {
            Log.d(TAG, "observe now playing")
        }

        playerViewModel.musicServiceConnection.playbackState.observe(this) {
            Log.d(TAG, "observe playback state : ${it.stateName}")
            if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                startUpdateSeekBar("onCreate")
                ivPlay.setImageResource(R.drawable.exo_controls_pause)
            } else if (it.state == PlaybackStateCompat.STATE_PAUSED
                || it.state == PlaybackStateCompat.STATE_NONE
            ) {
                stopUpdateSeekBar()
                ivPlay.setImageResource(R.drawable.exo_controls_play)
            }
        }

        ivPlay.setOnClickListener {
            val currentPlayable = PlaylistManager.getCurrentPlayable("MainActivity::onCreate")
            if (currentPlayable != null) {
                playerViewModel.playMedia(currentPlayable)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            tvSplash.visibility = View.VISIBLE
            delay(2000)
            tvSplash.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isLoaded) {
            RequestManager.getServiceApi(null).getSongInfo().enqueue(object : Callback<SongRes> {
                override fun onResponse(call: Call<SongRes>, response: Response<SongRes>) {
                    if (response.isSuccessful && response.body() != null) {
                        val res = response.body()!!
                        songRes = res
                        val playable = Playable(res.singer, res.album, res.title, res.duration, res.image, res.file, res.lyrics)

                        PlaylistManager.songList = ArrayList<Playable>().apply {
                            add(playable)
                        }
                        updateUi(res)
                        parseLyrics(res.lyrics)
                        isLoaded = true
                    }
                }

                override fun onFailure(call: Call<SongRes>, t: Throwable) {
                    isLoaded = false
                }
            })
        } else {
            val state: PlaybackStateCompat = playerViewModel.musicServiceConnection.playbackState.value ?: PLAYBACK_STATE_NONE
            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                startUpdateSeekBar()
            }
        }
    }

    override fun onStop() {
        stopUpdateSeekBar()
        super.onStop()
    }

    private fun updateUi(res: SongRes) {
        Glide.with(this).load(res.image).into(ivThumb)

        tvSongName.text = res.title
        tvArtistName.text = "${res.album} - ${res.singer}"
        tvTotalTime.text = DateUtils.formatElapsedTime(res.duration)
        seekBar.max = (res.duration * 1000).toInt()

        lyricView.setTitle(res.title)
    }

    private fun parseLyrics(lyric: String?) {
        if (lyric.isNullOrEmpty()) {
            return
        }

//        val regex = "\\[\\d{2}:\\d{2}:\\d{3}]".toRegex()
        val startDelimiter = "["
        val endDelimiter = "]"

        if (lyric.contains(startDelimiter)) {
            val eachLine = lyric.split(startDelimiter)

            var index = 0
            eachLine.forEach {
                if (it.contains(endDelimiter)) {
                    val splitData = it.split(endDelimiter)
                    lyricDataList.add(LyricData(index, TimeUtil.getMilliFromTime(splitData[0]), splitData[1]))
                    index++
                }
            }
        }
        lyricView.lyricDataList = lyricDataList
    }

    private fun expandWindow(isExpand: Boolean) {
        behavior.state = if (isExpand) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_HIDDEN
    }

    private val handler = Handler(Looper.getMainLooper())
    private val scheduleExecutor = Executors.newSingleThreadScheduledExecutor()
    private var scheduleFuture: ScheduledFuture<*>? = null

    private fun startUpdateSeekBar(reason: String? = null) {
        stopUpdateSeekBar()
        if (!scheduleExecutor.isShutdown) {
            scheduleFuture = scheduleExecutor.scheduleAtFixedRate({
                handler.post { updateProgress() }
            }, UPDATE_INITIAL_INTERNAL, UPDATE_INTERNAL, TimeUnit.MILLISECONDS)
        }
        lyricViewModel.run()
    }

    private fun updateProgress() {
        val currentPosition = playerViewModel.musicServiceConnection.playbackState.value?.currentPlayBackPosition?.toInt() ?: 0
        seekBar.progress = currentPosition
    }

    private fun stopUpdateSeekBar() {
        scheduleFuture?.cancel(false)
        lyricViewModel.cancel()
    }
}