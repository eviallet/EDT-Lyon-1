package com.gueg.edt.weekview

import android.widget.Toast
import com.gueg.edt.Course
import com.gueg.edt.EventCreator
import com.gueg.edt.weekview.data.WeekData
import com.gueg.edt.weekview.view.WeekView
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import java.util.*
import kotlin.collections.ArrayList

class WeekViewWrapper(private val weekView: WeekView) {

    private val weeks = ArrayList<WeekData>()
    private var currentWeek = 0
        set(value) {
            weekView.removeViews(1, weekView.childCount - 1)
            field = value
            weekView.addEvents(weeks[currentWeek])
        }

    fun loadWeeks(courses : List<Course>) {
        var currentCourse = 0

        while(currentCourse < courses.size) {
            val weekData = WeekData()
            val weekNumber = courses[currentCourse].weekNumber()

            while(courses[currentCourse].weekNumber() == weekNumber && currentCourse < courses.size) {
                weekData.add(EventCreator.createEventFromCourse(courses[currentCourse]))
                currentCourse++
            }

            weeks.add(weekData)
        }

        setWeek(LocalDate.now())
    }


    fun setWeek(week: Int) {
        this.currentWeek = week
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