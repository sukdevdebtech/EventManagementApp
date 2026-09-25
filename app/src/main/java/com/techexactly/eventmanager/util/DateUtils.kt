package com.techexactly.eventmanager.util

import com.techexactly.eventmanager.data.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun format(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))
}

/** Event time in epoch millis. */
val Event.timeMillis: Long get() = dateTime.toDate().time

fun Event.isUpcoming(nowMillis: Long): Boolean = timeMillis >= nowMillis
