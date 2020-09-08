package com.gueg.edt

import android.view.View
import android.widget.TextView
import com.kizitonwose.calendarview.ui.ViewContainer

class DayViewContainer(view: View) : ViewContainer(view) {
    val dayNumber: TextView = view.findViewById(R.id.calendarDayNumber)
    val dayText: TextView = view.findViewById(R.id.calendarDayText)
    val monthText: TextView = view.findViewById(R.id.calendarMonthText)
}