package com.gueg.edt

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gueg.edt.weekview.data.Event
import org.threeten.bp.format.DateTimeFormatter

class EventDialog(context: Context, private val event: Event.Single) : BottomSheetDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_event)

        val title = findViewById<TextView>(R.id.dialog_event_title)!!
        title.text = event.name

        val timeStart = findViewById<TextView>(R.id.dialog_event_time_start)!!
        timeStart.text = event.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        val timeEnd = findViewById<TextView>(R.id.dialog_event_time_end)!!
        timeEnd.text = event.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        val location = findViewById<TextView>(R.id.dialog_event_location)!!
        location.text = event.location

        val desciption = findViewById<TextView>(R.id.dialog_event_description)!!
        desciption.text = event.description

    }

}