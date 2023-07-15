package com.huanchengfly.tieba.post.ui.widgets.compose.video.util

import java.util.Locale
import java.util.concurrent.TimeUnit

fun getDurationString(durationMs: Long, negativePrefix: Boolean): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs)

    return if (hours > 0) {
        java.lang.String.format(
            Locale.getDefault(), "%s%02d:%02d:%02d",
            if (negativePrefix) "-" else "",
            hours,
            minutes - TimeUnit.HOURS.toMinutes(hours),
            seconds - TimeUnit.MINUTES.toSeconds(minutes)
        )
    } else java.lang.String.format(
        Locale.getDefault(), "%s%02d:%02d",
        if (negativePrefix) "-" else "",
        minutes,
        seconds - TimeUnit.MINUTES.toSeconds(minutes)
    )
}