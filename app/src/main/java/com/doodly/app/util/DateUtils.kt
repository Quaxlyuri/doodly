package com.doodly.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val zone: ZoneId = ZoneId.systemDefault()
private val displayFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")

fun todayMillis(): Long = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()

fun normalizeDate(millis: Long): Long =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        .atStartOfDay(zone).toInstant().toEpochMilli()

fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(displayFormatter)

fun localDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

fun monthRange(month: YearMonth): LongRange {
    val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    return start..end
}

fun dateMillis(date: LocalDate): Long =
    date.atStartOfDay(zone).toInstant().toEpochMilli()
