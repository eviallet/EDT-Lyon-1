package com.gueg.edt

import android.graphics.Color
import de.tobiasschuerg.weekview.data.Event
import java.util.*
import kotlin.collections.HashMap

object EventCreator {

    private var random = Random()

    private val coursesColor = HashMap<String, Int>()

    fun createEventFromCourse(course: Course) : Event.Single {

        if(!coursesColor.containsKey(course.name))
            coursesColor[course.name] = randomColor()

        return Event.Single(
            id = random.nextLong(),
            date = course.date,
            title = course.description,
            shortTitle = course.name,
            subTitle = course.location,
            startTime = course.startTime,
            endTime = course.endTime,
            textColor = Color.WHITE,
            backgroundColor = coursesColor[course.name]!!
        )
    }

    private fun randomColor(): Int {
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }
}