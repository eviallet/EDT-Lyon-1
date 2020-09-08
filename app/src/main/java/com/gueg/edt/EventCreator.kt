package com.gueg.edt

import android.graphics.Color
import com.gueg.edt.weekview.data.Event
import java.util.*
import kotlin.collections.HashMap

object EventCreator {

    private var random = Random()

    private val coursesColor = HashMap<String, Int>()

    private val colors = parseColors("#e79797", /*"#efb484",*/ "#efc989", /*"#c6c69c",*/ /* REPEAT */ "#b5eeb9", "#addfee", "#d2afea", "#efc989", "#c6c69c")
    private var currentColorIndex = 0

    fun createEventFromCourse(course: Course) : Event.Single {
        val key = course.name.substring(0, if(course.name.length >= 5) 5 else course.name.length)

        if(!coursesColor.containsKey(key))
            coursesColor[key] = colors[currentColorIndex++]

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

    private fun parseColors(vararg strs: String) : IntArray {
        val array = IntArray(strs.size)

        for(i in array.indices)
            array[i] = Color.parseColor(strs[i])

        return array
    }

}