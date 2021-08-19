package com.huanchengfly.tieba.post.utils

import android.content.Context
import com.huanchengfly.tieba.post.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    @JvmStatic
    fun getRelativeTimeString(
        context: Context,
        timestampString: String
    ): String {
        return getRelativeTimeString(context, fixTimestamp(timestampString))
    }

    @JvmStatic
    fun getRelativeTimeString(
        context: Context,
        timestamp: Long
    ): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = fixTimestamp(timestamp)
        }
        val currentCalendar = Calendar.getInstance()
        return if (currentCalendar.after(calendar)) {
            if (calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                if (calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
                    if (calendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH)) {
                        if (calendar.get(Calendar.HOUR_OF_DAY) == currentCalendar.get(Calendar.HOUR_OF_DAY)) {
                            if (calendar.get(Calendar.MINUTE) == currentCalendar.get(Calendar.MINUTE)) {
                                context.getString(
                                    R.string.relative_date_second,
                                    currentCalendar.get(Calendar.SECOND) - calendar.get(Calendar.SECOND)
                                )
                            } else {
                                context.getString(
                                    R.string.relative_date_minute,
                                    currentCalendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE)
                                )
                            }
                        } else {
                            context.getString(
                                R.string.relative_date_hour,
                                currentCalendar.get(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY)
                            )
                        }
                    } else {
                        context.getString(
                            R.string.relative_date_month,
                            currentCalendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                } else {
                    calendar.format(context.getString(R.string.pattern_date_short))
                }
            } else {
                calendar.format(context.getString(R.string.pattern_date_long))
            }
        } else {
            calendar.format(context.getString(R.string.relative_date_after))
        }
    }

    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    @JvmStatic
    fun isToday(timestamp: Long): Boolean {
        val date = DateFormat.getDateInstance().format(timestamp)
        val todayDate = DateFormat.getDateInstance().format(System.currentTimeMillis())
        return date == todayDate
    }

    private fun Calendar.format(pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timeInMillis))
    }

    private fun fixTimestamp(timestamp: Long): Long {
        return fixTimestamp(timestamp.toString())
    }

    private fun fixTimestamp(timestampString: String): Long {
        val timestampStrBuilder: StringBuilder = StringBuilder(timestampString)
        while (timestampStrBuilder.length < 13) {
            timestampStrBuilder.append("0")
        }
        return timestampStrBuilder.toString().toLong()
    }
}