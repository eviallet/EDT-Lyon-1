package com.gueg.edt.fab

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.ArcMotion
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gueg.edt.R
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class DateActivity : AppCompatActivity() {

    private lateinit var container: ViewGroup
    private lateinit var calendarView: CalendarView
    var selectedDate: LocalDate? = null
    var cells = ArrayList<DayCellContainer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        container = findViewById(R.id.activity_date_container)
        calendarView = findViewById(R.id.activity_date_picker)


        // ================================================================
        // ========================== DAY BINDER ==========================
        // ================================================================

        calendarView.dayBinder = object : DayBinder<DayCellContainer> {
            override fun create(view: View): DayCellContainer {
                val dcc = DayCellContainer(view)
                cells.add(dcc)
                return dcc
            }

            override fun bind(container: DayCellContainer, day: CalendarDay) {
                container.date = day
                container.textview.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    if(day.date.atTime(0, 0) == LocalDate.now().atTime(0, 0)) {
                        container.textview.setBackgroundResource(R.drawable.date_today)
                        container.textview.setTextColor(Color.WHITE)
                    } else {
                        container.textview.setTextColor(Color.BLACK)
                    }
                } else {
                    container.textview.setTextColor(Color.GRAY)
                }
            }
        }


        // ================================================================
        // ========================= MONTH BINDER =========================
        // ================================================================


        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                @SuppressLint("SetTextI18n") // Concatenation warning for `setText` call.
                container.textview.text = "${month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).toUpperCase(Locale.getDefault())} ${month.year}"
            }
        }

        // ================================================================
        // ======================= CALENDAR SETUP =========================
        // ================================================================

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth
        val lastMonth = currentMonth.plusMonths(8)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)


        // ================================================================
        // ======================== ACTIVITY SETUP ========================
        // ================================================================

        setupSharedEelementTransitions1()

        val dismissListener = View.OnClickListener { view: View? -> dismiss() }
        container.setOnClickListener(dismissListener)
        container.findViewById<View>(R.id.activity_date_close).setOnClickListener(dismissListener)
        container.findViewById<View>(R.id.activity_date_ok).setOnClickListener { v: View? ->
            val data = Intent()
            data.putExtra("YEAR", selectedDate?.year)
            data.putExtra("MONTH", selectedDate?.monthValue)
            data.putExtra("DAY", selectedDate?.dayOfMonth)
            setResult(RESULT_OK, data)
            finish()
        }

    }

    inner class DayCellContainer(view: View) : ViewContainer(view) {
        var date: CalendarDay? = null
        var textview: TextView = view.findViewById(R.id.calendar_day_picker_text)

        init {
            view.setOnClickListener {
                selectedDate = date!!.date
                while(selectedDate!!.dayOfWeek != DayOfWeek.MONDAY) {
                    selectedDate = selectedDate!!.minusDays(1)
                }

                for (dcc in cells) {
                    when {
                        isSameWeek(dcc.date?.date, selectedDate) -> {
                            when (dcc.date!!.date.dayOfWeek) {
                                DayOfWeek.MONDAY -> dcc.textview.setBackgroundResource(R.drawable.date_selected_start)
                                DayOfWeek.SUNDAY -> dcc.textview.setBackgroundResource(R.drawable.date_selected_end)
                                else -> dcc.textview.setBackgroundResource(R.drawable.date_selected_middle)
                            }
                        }
                        dcc.date?.date?.atTime(0, 0) == LocalDate.now().atTime(0, 0) -> {
                            dcc.textview.setBackgroundResource(R.drawable.date_today)
                            dcc.textview.setTextColor(Color.WHITE)
                        }
                        else -> {
                            dcc.textview.background = null
                        }
                    }
                }
            }
        }
    }


    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        var textview: TextView = view.findViewById(R.id.calendar_month)
    }


    fun isSameWeek(date1: LocalDate?, date2: LocalDate?): Boolean {
        if(date1 == null || date2 == null)
            return false

        return getWeekNumber(date1) == getWeekNumber(date2)
    }

    private fun getWeekNumber(date: LocalDate): Int {
        return date[WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()]
    }

    private fun setupSharedEelementTransitions1() {
        val arcMotion = ArcMotion()
        arcMotion.minimumHorizontalAngle = 50f
        arcMotion.minimumVerticalAngle = 50f
        val easeInOut =
            AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in)
        val sharedEnter = MorphFabToDialog()
        sharedEnter.pathMotion = arcMotion
        sharedEnter.interpolator = easeInOut
        val sharedReturn = MorphDialogToFab()
        sharedReturn.pathMotion = arcMotion
        sharedReturn.interpolator = easeInOut
        sharedEnter.addTarget(container)
        sharedReturn.addTarget(container)
        window.sharedElementEnterTransition = sharedEnter
        window.sharedElementReturnTransition = sharedReturn
    }

    fun setupSharedEelementTransitions2() {
        val arcMotion = ArcMotion()
        arcMotion.minimumHorizontalAngle = 50f
        arcMotion.minimumVerticalAngle = 50f
        val easeInOut =
            AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in)
        val sharedEnter = MorphTransition(
            resources.getColor(R.color.fab_background_color),
            resources.getColor(R.color.dialog_background_color),
            100,
            resources.getDimensionPixelSize(R.dimen.dialog_corners),
            true
        )
        sharedEnter.pathMotion = arcMotion
        sharedEnter.interpolator = easeInOut
        val sharedReturn = MorphTransition(
            resources.getColor(R.color.dialog_background_color),
            resources.getColor(R.color.fab_background_color),
            resources.getDimensionPixelSize(R.dimen.dialog_corners),
            100,
            false
        )
        sharedReturn.pathMotion = arcMotion
        sharedReturn.interpolator = easeInOut
        sharedEnter.addTarget(container)
        sharedReturn.addTarget(container)
        window.sharedElementEnterTransition = sharedEnter
        window.sharedElementReturnTransition = sharedReturn
    }

    override fun onBackPressed() {
        dismiss()
    }

    private fun dismiss() {
        setResult(RESULT_CANCELED)
        finishAfterTransition()
    }
}