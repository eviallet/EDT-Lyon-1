package com.gueg.edt

import android.util.Log
import android.widget.Toast
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.util.*
import kotlin.collections.ArrayList

object Parser {

    private const val SCHEDULE_FILENAME = "schedule.ics"
    private var activity: MainActivity ?= null

    fun with(activity: MainActivity) : Parser {
        this.activity = activity
        return this
    }

    fun shouldDownload() : Boolean {
        val file = File(activity!!.cacheDir, SCHEDULE_FILENAME)
        if(!file.exists())
            return true

        val millis = file.lastModified()
        val curTime = System.currentTimeMillis()

        val shouldUpdate = curTime - millis > 1000 * 3600 * 12
        Log.d(":-:", "shouldDownload = $shouldUpdate")

        return shouldUpdate
    }

    fun download(url: String, listener: DownloadListener) {
        Thread {
            val cn: URLConnection = URL(url).openConnection()
            cn.connect()
            val stream: InputStream = cn.getInputStream()
            val bytes = stream.readBytes()
            stream.close()

            val file = File(activity!!.cacheDir, SCHEDULE_FILENAME)
            if(file.exists())
                file.delete()

            file.createNewFile()
            file.writeBytes(bytes)

            listener.onDownloadFinished()
        }.start()
    }

    fun extract() : List<Course> {

        // ========== Load text from downloaded or cached file ==========

        val file = File(activity!!.cacheDir, SCHEDULE_FILENAME)
        val list = ArrayList<String>()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                if(line != null)
                    list.add(line as String)
            }
            br.close()
        } catch (e: IOException) {
            throw e
        }


        // ========== Extract all courses by interpreting each line ==========

        val courses = ArrayList<Course>()

        var endReached = false
        var currentLine = 0

        val offsetFromUtc: Long = TimeZone.getDefault().getOffset(Date().time)/(1000*3600).toLong()

        while(!endReached) {
            var line = list[currentLine]

            if(line == "BEGIN:VEVENT") {
                val course = Course()
                var lastTag = ""

                while(line != "END:VEVENT") {
                    line = list[++currentLine]
                    if(!line.contains(":")) {
                        line = line.substring(1, line.length).replace("\r\n", "")
                        when(lastTag) {
                            "SUMMARY" -> course.name += line
                            "LOCATION" -> course.location += line
                            "DESCRIPTION" -> course.description += line
                        }
                        continue
                    }

                    val tag = line.split(":")[0]
                    var value = line.split(":")[1].replace("\r\n", "").replace("\\,", ",")

                    when(tag) {
                        "DTSTART" -> {
                            val dateTime = LocalDateTime.parse(
                                value,
                                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                            )
                            course.date = dateTime.toLocalDate()
                            course.startTime = dateTime.toLocalTime().plusHours(offsetFromUtc)
                        }
                        "DTEND" -> {
                            val dateTime: LocalDateTime = LocalDateTime.parse(
                                value, DateTimeFormatter.ofPattern(
                                    "yyyyMMdd'T'HHmmss'Z'"
                                )
                            )
                            course.endTime = dateTime.toLocalTime().plusHours(offsetFromUtc)
                        }
                        "SUMMARY" -> course.name = value
                        "LOCATION" -> course.location = value
                        "DESCRIPTION" -> {
                            while (value.startsWith("\\n"))
                                value = value.replaceFirst("\\n", "")
                            course.description = value.replace("\\n", "  ")
                        }
                    }
                    lastTag = tag
                }

                course.description = course.description.substringBefore("(Exported")
                courses.add(course)
            } else {
                currentLine++
            }

            if(line.contains("END:VCALENDAR"))
                endReached = true
        }

        var tagCount = 0
        for(line in list)
            if((line.contains("BEGIN:VEVENT")))
                tagCount++
        Log.d(":-:","Parser : extracted ${courses.size} courses of $tagCount events")
        if(courses.size != tagCount)
            Toast.makeText(activity, "${tagCount - courses.size} cours non extraits !!!", Toast.LENGTH_SHORT).show()

        return courses
    }

    interface DownloadListener {
        fun onDownloadFinished()
    }
}