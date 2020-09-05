package com.gueg.edt

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime


class Course {
    lateinit var name: String
    lateinit var location: String
    lateinit var description: String
    lateinit var date: LocalDate
    lateinit var startTime: LocalTime
    lateinit var endTime: LocalTime
}