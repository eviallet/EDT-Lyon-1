package com.gueg.edt.weekview.util

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class ThreeTenInit : Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

    }
}