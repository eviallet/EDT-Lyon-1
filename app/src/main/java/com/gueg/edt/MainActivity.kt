package com.gueg.edt

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gueg.edt.weekview.WeekViewWrapper
import com.gueg.edt.weekview.view.WeekView
import java.io.File


class MainActivity : AppCompatActivity() {

    // TODO changer semaine
    // TODO grossir texte events
    // TODO sélectionner 1 jour = zoom

    fun isConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
    }

    private val URL_FILENAME = "url.txt"
    private var url: String = ""

    private val ACTIVITY_LOGIN = 0

    private lateinit var weekView : WeekView
    private lateinit var weekViewWrapper : WeekViewWrapper


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        Parser.with(this)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_refresh -> {
            weekView.removeViews(1, weekView.childCount - 1)
            updateCalendar()
            true
        }

        R.id.action_url -> {
            val editText = EditText(this)
            AlertDialog.Builder(this)
                .setView(editText)
                .setPositiveButton("Valider") { dialog, _ ->
                    url = editText.text.toString()
                    if (url.isNotEmpty()) {
                        writeUrlToFile()
                        updateCalendar()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Annuler") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()

            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginScreen::class.java)
        startActivityForResult(intent, ACTIVITY_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == ACTIVITY_LOGIN && resultCode == RESULT_OK) {
            url = data!!.getStringExtra(LoginScreen.ADE_URL_EXTRA)!!
            //updateCalendar()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}

