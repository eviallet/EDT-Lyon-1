package com.gueg.edt.weekview.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.gueg.edt.R
import com.gueg.edt.weekview.data.Event
import com.gueg.edt.weekview.data.EventConfig
import com.gueg.edt.weekview.data.WeekData
import com.gueg.edt.weekview.data.WeekViewConfig
import com.gueg.edt.weekview.util.Animation
import com.gueg.edt.weekview.util.DayOfWeekUtil
import com.gueg.edt.weekview.util.SwipeHelper
import com.gueg.edt.weekview.util.dipToPixelF
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.util.*
import kotlin.math.roundToInt

class WeekView(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    private val backgroundView: WeekBackgroundView
    private val overlapsWith = ArrayList<EventView>()

    private var isInScreenshotMode = false

    private var clickListener: ((view: EventView) -> Unit)? = null
    private var contextMenuListener: OnCreateContextMenuListener? = null
    private var eventTransitionName: String? = null

    private val accentColor: Int

    private val weekViewConfig: WeekViewConfig
    var swipeHelper: SwipeHelper?= null
    set(value) {
        field = value
        setOnTouchListener(swipeHelper)
    }

    private var eventConfig = EventConfig()

    init {
        val arr = context.obtainStyledAttributes(attributeSet, R.styleable.WeekView)
        accentColor = arr.getColor(R.styleable.WeekView_accent_color, Color.BLUE)
        arr.recycle() // Do this when done.

        val prefs = context.getSharedPreferences("ts_week_view", Context.MODE_PRIVATE)
        weekViewConfig = WeekViewConfig(prefs)

        backgroundView = WeekBackgroundView(context)
        backgroundView.setAccentColor(accentColor)
        backgroundView.scalingFactor = weekViewConfig.scalingFactor

        addView(backgroundView)
    }


    fun setLessonClickListener(clickListener: (view: EventView) -> Unit) {
        this.clickListener = clickListener
        for (childIndex in 0 until childCount) {
            val view: View = getChildAt(childIndex)
            if (view is EventView) {
                view.setOnClickListener {
                    clickListener.invoke(view)
                }
            }
        }
    }

    override fun setOnCreateContextMenuListener(contextMenuListener: OnCreateContextMenuListener?) {
        this.contextMenuListener = contextMenuListener
        for (childIndex in 0 until childCount) {
            val view: View = getChildAt(childIndex)
            if (view is EventView) {
                view.setOnCreateContextMenuListener(contextMenuListener)
            }
        }
    }

    fun addEvents(weekData: WeekData?) {
        if(weekData == null)
            return

        for (event in weekData.getSingleEvents())
            addEvent(event)
    }

    fun addEvent(event: Event.Single) {
        // enable weekend if not enabled yet
        when (event.date.dayOfWeek) {
            DayOfWeek.SATURDAY -> {
                if (!backgroundView.days.contains(DayOfWeek.SATURDAY)) {
                    backgroundView.days.add(DayOfWeek.SATURDAY)
                }
            }
            DayOfWeek.SUNDAY -> {
                if (!backgroundView.days.contains(DayOfWeek.SATURDAY)) {
                    backgroundView.days.add(DayOfWeek.SATURDAY)
                }
                if (!backgroundView.days.contains(DayOfWeek.SUNDAY)) {
                    backgroundView.days.add(DayOfWeek.SUNDAY)
                }
            }
            else -> {
                // nothing to do, just add the event
            }
        }

        val lv = EventView(context, event, eventConfig, weekViewConfig.scalingFactor)
        //backgroundView.updateTimes(event.startTime, event.endTime)

        // mark active event
        val now = LocalTime.now()
        if (LocalDate.now().dayOfWeek == event.date.dayOfWeek && event.startTime < now && event.endTime > now) {
            lv.animation = Animation.createBlinkAnimation()
        }

        lv.setOnClickListener { clickListener?.invoke(lv) }
        lv.setOnCreateContextMenuListener(contextMenuListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lv.transitionName = eventTransitionName
        }

        addView(lv)
    }

    fun removeEvents() {
        removeViews(1, childCount - 1)
    }

    fun enableBackgroundColorForDate(enable: Boolean) {
        backgroundView.shouldDrawDayHighilight = enable
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(true, l, t, r, b)
        if (isInScreenshotMode) {
            backgroundView.setScreenshotMode(true)
        }

        val saturdayEnabled = backgroundView.days.contains(DayOfWeek.SATURDAY)
        val sundayEnabled = backgroundView.days.contains(DayOfWeek.SUNDAY)

        for (childIndex in 0 until childCount) {
            val eventView: EventView
            val childView = getChildAt(childIndex)
            if (childView is EventView) {
                eventView = childView
            } else {
                continue
            }

            val column: Int = DayOfWeekUtil.mapDayToColumn(eventView.event.date.dayOfWeek, saturdayEnabled, sundayEnabled)
            if (column < 0) {
                // should not be necessary as wrong days get filtered before.
                childView.setVisibility(View.GONE)
                removeView(childView)
                continue
            }
            var left: Int = backgroundView.getColumnStart(column, true)
            val right: Int = backgroundView.getColumnEnd(column, true)

            overlapsWith.clear()
            for (j in 0 until childIndex) {
                val v2 = getChildAt(j)
                // get next LessonView
                if (v2 is EventView) {
                    // check for overlap
                    if (v2.event.date != eventView.event.date) {
                        continue // days differ, overlap not possible
                    } else if (overlaps(eventView, v2)) {
                        overlapsWith.add(v2)
                    }
                }
            }

            if (overlapsWith.size > 0) {
                val width = (right - left) / (overlapsWith.size + 1)
                for ((index, view) in overlapsWith.withIndex()) {
                    val left2 = left + index * width
                    view.layout(left2, view.top, left2 + width, view.bottom)
                }
                left = right - width
            }

            eventView.scalingFactor = weekViewConfig.scalingFactor
            val startTime = backgroundView.startTime
            val lessonStart = eventView.event.startTime
            val offset = Duration.between(startTime, lessonStart)

            val yOffset = offset.toMinutes() * weekViewConfig.scalingFactor
            val top = context.dipToPixelF(yOffset)

            val bottom = top + eventView.measuredHeight
            eventView.layout(left, top.roundToInt(), right, bottom.roundToInt())
        }
    }

    private fun overlaps(left: EventView, right: EventView): Boolean {
        val rightStartsAfterLeftStarts = right.event.startTime >= left.event.startTime
        val rightStartsBeforeLeftEnds = right.event.startTime < left.event.endTime
        val lessonStartsWithing = rightStartsAfterLeftStarts && rightStartsBeforeLeftEnds

        val leftStartsBeforeRightEnds = left.event.startTime < right.event.endTime
        val rightEndsBeforeOrWithLeftEnds = right.event.endTime <= left.event.endTime
        val lessonEndsWithing = leftStartsBeforeRightEnds && rightEndsBeforeOrWithLeftEnds

        val leftStartsAfterRightStarts = left.event.startTime > right.event.startTime
        val rightEndsAfterLeftEnds = right.event.endTime > left.event.endTime
        val lessonWithin = leftStartsAfterRightStarts && rightEndsAfterLeftEnds

        return lessonStartsWithing || lessonEndsWithing || lessonWithin
    }

}
