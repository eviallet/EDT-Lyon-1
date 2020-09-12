package com.gueg.edt.weekview.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.PaintDrawable
import android.view.ContextMenu.ContextMenuInfo
import android.view.View
import android.view.animation.AlphaAnimation
import com.gueg.edt.weekview.data.Event
import com.gueg.edt.weekview.data.EventConfig
import com.gueg.edt.weekview.util.TextHelper
import com.gueg.edt.weekview.util.dipToPixelF
import com.gueg.edt.weekview.util.dipToPixelI
import kotlin.math.min
import kotlin.math.roundToInt


/** this view is only constructed during runtime. */
@SuppressLint("ViewConstructor")
class EventView(
    context: Context,
    val event: Event.Single,
    val config: EventConfig,
    var scalingFactor: Float = 1f

) : View(context) {

    private val CORNER_RADIUS_PX = context.dipToPixelF(2f)

    private val textPaint: Paint by lazy { Paint().apply { isAntiAlias = true } }

    private val subjectName: String by lazy { if (config.useShortNames) event.name else event.description }

    private val textBounds: Rect = Rect()

    private val weightSum: Int
    private val weightStartTime: Int
    private val weightUpperText: Int
    private val weightTitle = 3
    private val weightSubTitle: Int
    private val weightLowerText: Int
    private val weightEndTime: Int

    init {
        val padding = this.context.dipToPixelI(2f)
        setPadding(padding, padding, padding, padding)

        background = PaintDrawable().apply {
            paint.color = event.backgroundColor
            setCornerRadius(CORNER_RADIUS_PX)
        }

        /** Calculate weights above & below. */
        weightStartTime = if (config.showTimeStart) 1 else 0
        weightUpperText = if (config.showUpperText) 1 else 0
        weightSubTitle = if (config.showSubtitle) 1 else 0
        weightLowerText = if (config.showLowerText) 1 else 0
        weightEndTime = if (config.showTimeEnd) 1 else 0

        weightSum = weightStartTime + weightUpperText + weightSubTitle + weightLowerText + weightEndTime + weightTitle

        textPaint.color = event.textColor
    }

    override fun onDraw(canvas: Canvas) {

        // description
        val maxTextSize = TextHelper.fitText(
            subjectName,
            textPaint.textSize * 3,
            width - (paddingLeft + paddingRight),
            height / 4
        )
        textPaint.textSize = maxTextSize

        var weight = weightStartTime + weightUpperText
        if (weight == 0) {
            weight++
        }
        val subjectY = getY(weight, weightTitle, textBounds)
        textPaint.getTextBounds(subjectName, 0, subjectName.length, textBounds)

        if(textBounds.width() > width) {
            var drawnText = subjectName
            while(textBounds.width() > width) {
                drawnText = drawnText.substring(0, drawnText.length - 1)
                textPaint.getTextBounds(drawnText, 0, drawnText.length, textBounds)
            }
            val endText = subjectName.substring(drawnText.length, drawnText.length + min(subjectName.length - drawnText.length, drawnText.length)).dropLast(3).plus("...")

            canvas.drawText(drawnText, 5f, 0.98f * subjectY.toFloat() - textBounds.height() / 2, textPaint)

            if(endText.length > 4)
                canvas.drawText(endText, 5f, 0.98f * subjectY.toFloat() + textBounds.height() / 2, textPaint)
        } else {
            canvas.drawText(subjectName, 5f, subjectY.toFloat(), textPaint)
        }


        textPaint.textSize = TextHelper.fitText(
            "123456", maxTextSize, width / 2,
            getY(position = 1, bounds = textBounds) - getY(position = 0, bounds = textBounds)
        )

        textPaint.textAlign = Paint.Align.LEFT

        // start time
        if (config.showTimeStart) {
            val startText = event.startTime.toString()
            textPaint.getTextBounds(startText, 0, startText.length, textBounds)
            canvas.drawText(
                startText,
                (textBounds.left + paddingLeft).toFloat(),
                (textBounds.height() + paddingTop).toFloat(),
                textPaint
            )
        }

        // end time
        if (config.showTimeEnd) {
            val endText = event.endTime.toString()
            textPaint.getTextBounds(endText, 0, endText.length, textBounds)
            canvas.drawText(
                endText,
                (width - (textBounds.right + paddingRight)).toFloat(),
                (height - paddingBottom).toFloat(),
                textPaint
            )
        }

        // subtitle = location
        textPaint.textAlign = Paint.Align.LEFT
        if (config.showSubtitle && event.location != null) {
            textPaint.getTextBounds(event.location, 0, event.location.length, textBounds)
            val teacherY = getY(
                position = weightStartTime + weightUpperText + weightTitle,
                bounds = textBounds
            )
            canvas.drawText(
                event.location,
                5f, //(width / 2 - textBounds.centerX()).toFloat(),
                1.02f * teacherY.toFloat(),
                textPaint
            )
        }

    }

    private fun getY(position: Int, weight: Int = 1, bounds: Rect): Int {
        val content = height - (paddingTop + paddingBottom)
        val y = (content * (position + 0.5f * weight) / weightSum) + paddingTop
        return y.roundToInt() - bounds.centerY()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeightDp = event.duration.toMinutes() * scalingFactor
        val desiredHeightPx = context.dipToPixelI(desiredHeightDp)
        val resolvedHeight = resolveSize(desiredHeightPx, heightMeasureSpec)

        setMeasuredDimension(width, resolvedHeight)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val anim = AlphaAnimation(0f, 1f)
        anim.fillAfter = true
        anim.duration = 400
        this.startAnimation(anim)
    }

    override fun getContextMenuInfo(): ContextMenuInfo {
        return LessonViewContextInfo(event)
    }

    data class LessonViewContextInfo(var event: Event.Single) : ContextMenuInfo
}
