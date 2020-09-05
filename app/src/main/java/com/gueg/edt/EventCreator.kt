package com.gueg.edt

import android.graphics.Color
import com.gueg.edt.weekview.data.Event
import java.util.*
import kotlin.collections.HashMap

object EventCreator {

    private var random = Random()

    private val coursesColor = HashMap<String, Int>()

    private val colors = parseColors("#58afbf", "#35b9bd", "#bbbbbb", "#58afbf", "#58afbf")
    private var currentColorIndex = 0

    fun createEventFromCourse(course: Course) : Event.Single {

        if(!coursesColor.containsKey(course.name))
            coursesColor[course.name] = colors[currentColorIndex]++


        return Event.Single(
            id = random.nextLong(),
            date = course.date,
            description = course.description,
            name = course.name,
            location = course.location,
            startTime = course.startTime,
            endTime = course.endTime,
            textColor = Color.WHITE,
            backgroundColor = coursesColor[course.name]!!
        )
    }

    private fun parseColors(vararg strs: String) : IntArray {
        val array = IntArray(strs.size)

        for(i in array.indices)
            array[i] = Color.parseColor(strs[i])

        return array
    }

}