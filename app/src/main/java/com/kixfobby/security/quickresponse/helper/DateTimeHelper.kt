package com.kixfobby.security.quickresponse.helper

import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi
import com.kixfobby.security.quickresponse.helper.DateTimeHelper

class DateTimeHelper {
    @get:RequiresApi(api = Build.VERSION_CODES.N)
    val current: String
        get() {
            val calendar = Calendar.getInstance()
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val day = calendar[Calendar.DAY_OF_MONTH]
            val hour = calendar[Calendar.HOUR]
            val minute = calendar[Calendar.MINUTE]
            val second = calendar[Calendar.SECOND]
            val ampm = calendar[Calendar.AM_PM]
            val yr = intToString(year, 4)
            val mn = intToString(month + 1, 2)
            val dy = intToString(day, 2)
            val hr = intToString(hour, 2)
            val min = intToString(minute, 2)
            val sec = intToString(second, 2)
            return if (ampm == Calendar.AM) {
                "$dy-$mn-$yr at $hr:$min:$sec AM"
            } else {
                "$dy-$mn-$yr at $hr:$min:$sec PM"
            }
        }

    companion object {
        fun intToString(num: Int, digits: Int): String {
            var output = num.toString()
            while (output.length < digits) output = "0$output"
            return if (output == "00") "12" else output
        }
    }
}