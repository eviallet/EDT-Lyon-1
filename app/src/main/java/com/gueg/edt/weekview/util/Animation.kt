package com.gueg.edt.weekview.util

import android.view.animation.AlphaAnimation
import android.view.animation.Animation

internal object Animation {

    fun createBlinkAnimation(): Animation {
        val blinkAnimation = AlphaAnimation(0.4f, 1.0f)
        blinkAnimation.duration = 200 // manage the time of the blink
        blinkAnimation.startOffset = 50
        blinkAnimation.repeatMode = Animation.REVERSE
        blinkAnimation.repeatCount = Animation.INFINITE
        return blinkAnimation
    }
}