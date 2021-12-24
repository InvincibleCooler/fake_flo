package com.android.fake.flo.fakeflo.utils

import android.annotation.SuppressLint
import android.util.Log


/**
 * Copyright (C) 2021 Kakao Inc. All rights reserved.
 *
 * Created by Invincible on 22/12/2021
 *
 */
class TimeUtil {
    companion object {
        private const val TAG = "TimeUtil"

        /**
         * dateFormat : "mm:ss:SSS"
         * 00:16:200
         */
        @SuppressLint("SimpleDateFormat")
        fun getMilliFromTime(dateFormat: String): Long {
            if (dateFormat.length != 9) {
                return 0
            }

            val minute = dateFormat.substring(0, 2).toInt()
            val second = dateFormat.substring(3, 5).toInt()
            val millisecond = dateFormat.substring(6, 9).toInt()

            return (minute * 60 * 1000 + second * 1000 + millisecond).toLong()
        }
    }
}