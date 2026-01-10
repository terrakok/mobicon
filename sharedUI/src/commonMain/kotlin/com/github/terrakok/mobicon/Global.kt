package com.github.terrakok.mobicon

import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import kotlinx.datetime.LocalDate

internal const val WIDE_SIZE = 800
internal const val DEBUG = false
internal val Log = object : co.touchlab.kermit.Logger(
    config = loggerConfigInit(
        platformLogWriter(NoTagFormatter),
        minSeverity = if (DEBUG) Severity.Verbose else Severity.Error,
    ),
    tag = "MobiCon"
) {}

internal fun LocalDate.dayShortString(): String {
    val date = this
    val dayOfWeek = date.dayOfWeek.name.lowercase().take(3).replaceFirstChar { it.uppercase() }
    val month = date.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }
    return "$dayOfWeek, $month ${date.day}"
}
