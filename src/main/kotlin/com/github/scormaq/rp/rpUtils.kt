package com.github.scormaq.rp

import com.epam.reportportal.listeners.*
import com.epam.reportportal.service.*
import com.epam.ta.reportportal.ws.model.log.*
import cucumber.api.Result
import gherkin.ast.Tag
import java.io.File
import java.lang.System.lineSeparator
import java.util.Calendar
import java.util.HashSet

enum class LogLevel {
    ERROR, WARN, INFO, DEBUG, TRACE, FATAL, UNKNOWN;
}

internal fun extractTags(tags: MutableCollection<Tag>?): HashSet<String>? = tags?.map { it.name }?.toHashSet()

internal fun mapCucumberStatus(cucumberStatus: Result.Type): String {
    return when (cucumberStatus) {
        Result.Type.PASSED -> Statuses.PASSED
        Result.Type.SKIPPED -> Statuses.SKIPPED
        else -> Statuses.FAILED
    }
}

internal fun sendFailure(result: Result, file: File? = null) {
    var errorMsg = "${result.error}\n"
    errorMsg += result.error.stackTrace.reversed().joinToString(lineSeparator())
    sendLog(errorMsg, LogLevel.ERROR, file)
}

internal fun sendLog(text: String = "", logLevel: LogLevel = LogLevel.INFO, file: File? = null) {
    val saveLog = SaveLogRQ().apply {
        level = logLevel.name
        logTime = Calendar.getInstance().time
        message = text
    }
    file?.let {
        val data = SaveLogRQ.File()
        data.name = it.name
        data.content = it.readBytes()
        saveLog.file = data
    }
    ReportPortal.emitLog { item ->
        saveLog.testItemId = item
        saveLog
    }
}