package com.gueg.edt.weekview.data

import android.widget.Toast
import com.gueg.edt.Course
import com.gueg.edt.EventCreator
import com.gueg.edt.weekview.view.WeekView
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import java.util.*
import kotlin.collections.HashMap

class WeekViewWrapper(private val weekView: WeekView) {

    private val weeks = HashMap<Int, WeekData>()
    private var currentWeek = 0
        set(value) {
            weekView.removeEvents()
            field = value
            weekView.addEvents(weeks[currentWeek])
        }

    fun loadWeeks(courses : List<Course>) {
        for(i in 1..52)
            weeks[i] = WeekData()

        var currentCourse = 0

        while(currentCourse < courses.size) {
            val weekNumber = courses[currentCourse].weekNumber()

            weeks[weekNumber]!!.add(EventCreator.createEventFromCourse(courses[currentCourse]))
            currentCourse++
        }
    }


    fun setWeek(week: Int) {
        this.currentWeek = week
        weekView.enableBackgroundColorForDate(week == LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()))
    }

    fun setWeek(date: LocalDate) {
        this.currentWeek = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
    }

    fun previousWeek() {
        if(currentWeek - 1 < 0) {
            Toast.makeText(weekView.context, "Aucun évènement avant cette date.", Toast.LENGTH_SHORT).show()
            return
        }

        currentWeek--
    }

    fun nextWeek() {
        if(currentWeek + 1 > weeks.size) {
            Toast.makeText(weekView.context, "Aucun évènement après cette date.", Toast.LENGTH_SHORT).show()
            return
        }

        currentWeek++
    }

}