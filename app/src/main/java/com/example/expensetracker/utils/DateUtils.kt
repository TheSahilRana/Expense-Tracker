package com.example.studysync.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(millis: Long?): String {
    if (millis == null) {
        return "Select Due Date"
    }
    val formatter = SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    )
    return formatter.format(Date(millis))
}