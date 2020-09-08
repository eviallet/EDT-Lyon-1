package com.gueg.edt

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.gueg.edt.fab.DateActivity
import com.gueg.edt.weekview.data.WeekViewWrapper
import com.gueg.edt.weekview.view.WeekView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*


class MainActivity : AppCompatActivity() {

    // TODO grossir texte events
    // TODO sélectionner 1 jour = zoom
    // TODO calendrier via fab
    // TODO non connecté à internet
    // TODO si janvier, charger depuis septembre d'avant (année scolaire)

    fun isConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
    }


    private val URL_FILENAME = "url.txt"
    private var url: String = ""

    private val ACTIVITY_LOGIN = 0
    private val ACTIVITY_DATE = 1

    private lateinit var weekView : WeekView
    private lateinit var weekViewWrapper : WeekViewWrapper

    private lateinit var calendarView: CalendarView


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ====================================================================================
        // =================                     FAB                        ===================
        // ====================================================================================

        val fab = findViewById<FloatingActionButton>(R.id.btnCalendar)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, DateActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                this@MainActivity,
                fab,
                getString(R.string.transition_dialog)
            )
            startActivityForResult(intent, ACTIVITY_DATE, options.toBundle())
        }

        // ====================================================================================
        // =================                   WEEKVIEW                     ===================
        // ====================================================================================

        weekView = findViewById(R.id.weekView)
        weekViewWrapper = WeekViewWrapper(weekView)

        // add an onClickListener for each event
        weekView.setLessonClickListener { eventView ->
            val dialog = EventDialog(this, eventView.event)
            dialog.show()
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }


        // ====================================================================================
        // =================                 CALENDARVIEW                   ===================
        // ====================================================================================

        calendarView = findViewById(R.id.calendarView)
        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                if(day.date.dayOfWeek == DayOfWeek.SATURDAY || day.date.dayOfWeek == DayOfWeek.SUNDAY) {
                    container.dayText.text = ""
                    container.dayNumber.text = ""
                    container.monthText.text = ""
                } else {
                    var dayText = day.date.dayOfWeek.getDisplayName(
                        TextStyle.SHORT,
                        Locale.getDefault()
                    )
                    dayText = dayText.substring(0, dayText.length - 1)
                    var monthText = day.date.month.getDisplayName(
                        TextStyle.SHORT,
                        Locale.getDefault()
                    )
                    monthText = monthText.substring(0, monthText.length - 1)
                    container.dayText.text = dayText
                    container.dayNumber.text = day.date.dayOfMonth.toString()
                    container.monthText.text = monthText
                }

            }
        }
        calendarView.updateMonthConfiguration(
            inDateStyle = InDateStyle.ALL_MONTHS,
            maxRowCount = 1,
            hasBoundaries = false
        )
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToDate(LocalDate.now())

        calendarView.monthScrollListener = { month ->
            weekViewWrapper.setWeek(
                month.weekDays[0][0].date.get(
                    WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()
                )
            )
        }


        // ====================================================================================
        // =================                    PARSER                      ===================
        // ====================================================================================

        Parser.with(this)

        Log.d(":-:", "MainActivity - onCreate")

        // if activity not returning from ACTIVITY_LOGIN
        if (intent != null) {
            if(intent.data != null) {
                if(intent.getStringExtra(LoginActivity.ADE_URL_EXTRA) != null) {
                    return
                }
            }
        }

        Log.d(":-:", "MainActivity - onCreate - reading URL from cache")

        if(!readUrlFromFile()) {
            startLoginActivity()
            return
        }

        if(Parser.shouldDownload())
            updateCalendar()
        else
            updateWeekView()
    }

    private fun updateCalendar() {
        Parser.download(url, object : Parser.DownloadListener {
            override fun onDownloadFinished() {
                updateWeekView()
            }
        })
    }

    fun updateWeekView() {
        runOnUiThread {
            val courses = Parser.extract()
            weekViewWrapper.loadWeeks(courses)
        }
    }

    private fun readUrlFromFile() : Boolean {
        val file = File(cacheDir, URL_FILENAME)

        if(!file.exists())
            return false

        url = file.readText()

        return url.isNotEmpty()
    }

    private fun writeUrlToFile() {
        val file = File(cacheDir, URL_FILENAME)

        if(file.exists())
            file.delete()

        file.createNewFile()

        file.writeText(url)
    }

    private fun removeUrlFile() {
        val file = File(cacheDir, URL_FILENAME)

        if(file.exists())
            file.delete()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_refresh -> {
            weekView.removeEvents()
            updateCalendar()
            true
        }

        R.id.action_url -> {
            removeUrlFile()
            startLoginActivity()

            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, ACTIVITY_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == ACTIVITY_LOGIN && resultCode == RESULT_OK) {
            url = data!!.getStringExtra(LoginActivity.ADE_URL_EXTRA)!!

            Log.d(":-:", "MainActivity - onActivityResult")

            writeUrlToFile()
            updateCalendar()
        }

        if(requestCode == ACTIVITY_DATE && resultCode == RESULT_OK) {

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}

