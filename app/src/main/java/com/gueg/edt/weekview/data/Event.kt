package com.gueg.edt.weekview.data

import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

sealed class Event {

    abstract val id: Long
    abstract val date: LocalDate
    abstract val description: String
    abstract val name: String

    data class Single(
        override val id: Long,
        override val date: LocalDate,
        override val description: String,
        override val name: String,
        val location: String? = null,

        val startTime: LocalTime,
        val endTime: LocalTime,

        val upperText: String? = null,
        val lowerText: String? = null,

        val textColor: Int,
        val backgroundColor: Int
    ) : Event() {
        val duration: Duration = Duration.between(startTime, endTime)
    }

    data class AllDay(
        override val id: Long,
        override val date: LocalDate,
        override val description: String,
        override val name: String
    ) : Event()

    data class MultiDay(
        override val id: Long,
        override val date: LocalDate,
        override val description: String,
        override val name: String,

        val lastDate: LocalDate
    ) : Event()
}