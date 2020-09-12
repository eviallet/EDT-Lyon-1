package com.gueg.edt

import android.content.Context
import android.graphics.Color
import com.gueg.edt.weekview.data.Event
import java.util.*
import kotlin.collections.HashMap

object EventCreator {

    var context: Context ?= null
    set(value) {
        field = value
        colors = value!!.resources.getIntArray(R.array.weekview_colors)
    }

    private var random = Random()

    private val coursesColor = HashMap<String, Int>()

    private var colors: IntArray ?= null
    private var currentColorIndex = 0

    fun createEventFromCourse(course: Course) : Event.Single {
        val key = course.name.substring(0, if(course.name.length >= 5) 5 else course.name.length)

        if(!coursesColor.containsKey(key))
            coursesColor[key] = colors!![currentColorIndex++]

        return Event.Single(
            id = random.nextLong(),
            date = course.date,
            description = course.description,
            name = course.name,
            location = course.location,
            startTime = course.startTime,
            endTime = course.endTime,
            textColor = Color.BLACK,
            backgroundColor = coursesColor[key]!!
        )
    }

}