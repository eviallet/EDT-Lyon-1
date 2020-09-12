package com.gueg.edt.weekview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.gueg.edt.weekview.util.DayOfWeekUtil
import com.gueg.edt.weekview.util.dipToPixelF
import com.gueg.edt.weekview.util.dipToPixelI
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import kotlin.math.roundToInt

internal class WeekBackgroundView constructor(context: Context) : View(context) {

    private val accentPaint: Paint by lazy {
        Paint().apply { strokeWidth = DIVIDER_WIDTH_PX.toFloat() * 2 }
    }

    private val paintDivider: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            strokeWidth = DIVIDER_WIDTH_PX.toFloat()
            color = DIVIDER_COLOR
        }
    }
    private val mPaintLabels: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.GRAY
            textSize = context.dipToPixelF(12f)
            textAlign = Paint.Align.CENTER
        }
    }

    private var isInScreenshotMode = false
    private val leftOffset: Int = context.dipToPixelI(48f)

    var shouldDrawDayHighilight: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    val days: MutableList<DayOfWeek> = DayOfWeekUtil.createList()
            .toMutableList()
            .apply {
                remove(DayOfWeek.SATURDAY)
                remove(DayOfWeek.SUNDAY)
            }

    var startTime: LocalTime = LocalTime.of(7, 0)
        private set
    private var endTime: LocalTime = LocalTime.of(20, 0)

    var scalingFactor = 1f
        set(value) {
            field = value
            requestLayout()
            // invalidate()
        }

    fun setAccentColor(color: Int) {
        accentPaint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)

        canvas.drawHorizontalDividers()
        canvas.drawColumnsWithHeaders()

        if (!isInScreenshotMode && !isInEditMode) {
            drawNowIndicator(canvas)
        }
    }

    private fun drawNowIndicator(canvas: Canvas) {
        if (startTime.isBefore(LocalTime.now()) && endTime.isAfter(LocalTime.now())) {
            val nowOffset = Duration.between(startTime, LocalTime.now())

            val minutes = nowOffset.toMinutes()
            val y = context.dipToPixelF(minutes * scalingFactor)
            accentPaint.alpha = 200
            canvas.drawLine(0f, y, leftOffset*1f, y, accentPaint)
        }
    }

    private fun Canvas.drawHorizontalDividers() {
        var localTime = startTime
        var last = LocalTime.MIN
        while (localTime.isBefore(endTime) && !last.isAfter(localTime)) {
            val offset = Duration.between(startTime, localTime)
            val y = context.dipToPixelF(offset.toMinutes() * scalingFactor)
            drawLine(0f, y, width.toFloat(), y, paintDivider)

            val timeString = localTime.toString()
            drawMultiLineText(this, timeString, context.dipToPixelF(25f), y + context.dipToPixelF(20f), mPaintLabels)

            last = localTime
            localTime = localTime.plusHours(1)
        }
        val offset = Duration.between(startTime, localTime)
        drawLine(0f, bottom.toFloat(), width.toFloat(), bottom.toFloat(), paintDivider)
    }

    private fun Canvas.drawColumnsWithHeaders() {
        val todayDay: DayOfWeek = LocalDate.now().dayOfWeek
        for ((column, dayId) in days.withIndex()) {
            drawLeftColumnDivider(column)
            if (dayId == todayDay && shouldDrawDayHighilight) {
                drawDayHighlight(column)
            }
        }
    }


    private fun Canvas.drawLeftColumnDivider(column: Int) {
        val left: Int = getColumnStart(column, false)
        drawLine(left.toFloat(), 0f, left.toFloat(), bottom.toFloat(), paintDivider)
    }

    private fun Canvas.drawDayHighlight(column: Int) {
        val left2: Int = getColumnStart(column, true)
        val right: Int = getColumnEnd(column, true)
        val rect = Rect(left2, 0, right, bottom)
        accentPaint.alpha = 32
        drawRect(rect, accentPaint)
    }

    private fun drawMultiLineText(canvas: Canvas, text: String, initialX: Float, initialY: Float, paint: Paint) {
        var currentY = initialY
        text.split(" ")
                .dropLastWhile(String::isEmpty)
                .forEach {
                    canvas.drawText(it, initialX, currentY, paint)
                    currentY += (-paint.ascent() + paint.descent()).toInt()
                }
    }


    internal fun getColumnStart(column: Int, considerDivider: Boolean): Int {
        val contentWidth: Int = width - leftOffset
        var offset: Int = leftOffset + contentWidth * column / days.size
        if (considerDivider) {
            offset += (DIVIDER_WIDTH_PX / 2)
        }
        return offset
    }

    internal fun getColumnEnd(column: Int, considerDivider: Boolean): Int {
        val contentWidth: Int = width - leftOffset
        var offset: Int = leftOffset + contentWidth * (column + 1) / days.size
        if (considerDivider) {
            offset -= (DIVIDER_WIDTH_PX / 2)
        }
        return offset
    }

    override fun onMeasure(widthMeasureSpec: Int, hms: Int) {
        val height = context.dipToPixelF(getDurationMinutes() * scalingFactor) + paddingBottom
        val heightMeasureSpec2 = MeasureSpec.makeMeasureSpec(height.roundToInt(), MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec2)
    }

    fun setScreenshotMode(screenshotMode: Boolean) {
        isInScreenshotMode = screenshotMode
    }


    private fun getDurationMinutes(): Long {
        return Duration.between(startTime, endTime).toMinutes()
    }

    companion object {
        /** Thickness of the grid.
         * Should be a multiple of 2 because of rounding. */
        private const val DIVIDER_WIDTH_PX: Int = 2
        private const val DIVIDER_COLOR = Color.LTGRAY
    }
}
