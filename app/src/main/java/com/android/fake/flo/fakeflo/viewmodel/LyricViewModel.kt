package com.android.fake.flo.fakeflo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.fake.flo.fakeflo.ext.currentPlayBackPosition
import kotlinx.coroutines.*


class LyricViewModel(private val playerViewModel: PlayerViewModel) : ViewModel() {
    companion object {
        private const val TAG = "LyricViewModel"
    }

    private lateinit var calculatePositionJob: Job

    var lyricDataList: MutableList<LyricData> = ArrayList()

    val position = MutableLiveData<Int>().apply {
        postValue(0)
    }

    override fun onCleared() {
        super.onCleared()
        cancel()
    }

    fun cancel() {
        if (::calculatePositionJob.isInitialized) {
            calculatePositionJob.cancel()
        }
    }

    fun run() {
        cancel()

        calculatePositionJob = viewModelScope.launch {
            while (isActive) {
                val pos = playerViewModel.musicServiceConnection.playbackState.value?.currentPlayBackPosition ?: 0
                findPosition(pos)
                delay(500)
            }
        }
    }

    private suspend fun findPosition(pos: Long) = withContext(Dispatchers.Default) {
        val indexList = ArrayList<Long>()
        for (i in 0 until lyricDataList.size) {
            val lyricData = lyricDataList[i]
            val time = lyricData.time
            if (time < pos) {
                indexList.add(time)
            } else {
                break
            }
        }
        val maxTime = indexList.maxOrNull()
        if (maxTime != null) {
            val data = lyricDataList.find {
                maxTime == it.time
            }
            position.postValue(data?.index ?: 0)
        }
    }

    class Factory(private val playerViewModel: PlayerViewModel) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LyricViewModel(playerViewModel) as T
        }
    }
}