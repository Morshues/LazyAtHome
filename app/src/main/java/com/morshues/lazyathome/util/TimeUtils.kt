package com.morshues.lazyathome.util

import java.util.Locale

fun formatDuration(seconds: Float, padLevel: Int = 0): String {
    val totalSeconds = seconds.toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60

    return when {
        hours > 0 || padLevel > 2 ->
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
        minutes > 0 || padLevel > 1 ->
            String.format(Locale.getDefault(), "%d:%02d", minutes, secs)
        else -> String.format(Locale.getDefault(), "%d", secs)
    }
}

fun formatDurationMS(seconds: Long, padLevel: Int = 0): String {
    return formatDuration(seconds / 1000f, padLevel)
}

fun formatDurationMSPair(currentSec: Long, totalSec: Long): String {
    val padLevel = if (totalSec > 3600) 2 else if (totalSec > 60) 1 else 0
    val total = formatDurationMS(totalSec, padLevel)
    val current = formatDurationMS(currentSec, padLevel)
    return "$current / $total"
}