package com.github.terrakok.mobicon

import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import kotlinx.datetime.TimeZone

internal val TZ get() = TimeZone.currentSystemDefault()
internal const val DEBUG = true
internal val Log = object : co.touchlab.kermit.Logger(
    config = loggerConfigInit(
        platformLogWriter(NoTagFormatter),
        minSeverity = if (DEBUG) Severity.Verbose else Severity.Error,
    ),
    tag = "MobiCon"
) {}