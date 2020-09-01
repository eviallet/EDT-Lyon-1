package com.gueg.edt

import android.content.Context
import android.util.Log
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.*
import java.net.URL
import java.net.URLConnection

object Parser {

    private const val SCHEDULE_FILENAME = "schedule.ics"
    private var context: Context ?= null



    fun with(context: Context) : Parser {
        this.context = context
        return this
    }

    fun shouldDownload() : Boolean {
        val file = File(context!!.cacheDir, SCHEDULE_FILENAME)
        if(!file.exists())
            return true

        val millis = file.lastModified()
        val curTime = System.currentTimeMillis()

        Log.d(":-:","shouldDownload = " + (curTime - millis > 1000 * 60 * 24 * 2))

        return curTime - millis > 1000 * 60 * 24 * 2 // true if last update was more than 2 days ago
    }

    fun download(url : String, listener: DownloadListener) {
        if(url.isEmpty())
            return

        Thread {
            val cn: URLConnection = URL(url).openConnection()
            cn.connect()
            val stream: InputStream = cn.getInputStream()
            val bytes = stream.readBytes()
            stream.close()

            val file = File(context!!.cacheDir, SCHEDULE_FILENAME)
            if(file.exists())
                file.delete()

            file.createNewFile()
            file.writeBytes(bytes)

            listener.onDownloadFinished()
        }.start()
    }

    fun extract() : List<Course> {

        // ========== Load text from downloaded or cached file ==========

        val file = File(context!!.cacheDir, SCHEDULE_FILENAME)
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

        var shouldRead = true
        var currentLine = 0

        while(shouldRead) {
            var line = list[currentLine]

            if(line == "BEGIN:VEVENT") {
                val course = Course()
                var lastTag = ""

                while(line != "END:VEVENT") {
                    line = list[currentLine++]
                    if(!line.contains(":")) {
                        line = line.substring(1,line.length).replace("\r\n","")
                        when(lastTag) {
                            "SUMMARY" -> course.name += line
                            "LOCATION" -> course.location += line
                            "DESCRIPTION" -> course.description += line
                        }
                        continue
                    }

                    val tag = line.split(":")[0]
                    var value = line.split(":")[1].replace("\r\n","")

                    when(tag) {
                        "DTSTART" -> {
                            val dateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))
                            course.date = dateTime.toLocalDate()
                            course.startTime = dateTime.toLocalTime()
                        }
                        "DTEND" -> {
                            val dateTime : LocalDateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))
                            course.endTime = dateTime.toLocalTime()
                        }
                        "SUMMARY" -> course.name = value
                        "LOCATION" -> course.location = value
                        "DESCRIPTION" -> {
                            while(value.startsWith("\\n"))
                                value = value.replaceFirst("\\n","")
                            course.description = value.substring(0, value.indexOf("(Exported")).replace("\\n","  ")
                        }
                    }
                    lastTag = tag
                }

                courses.add(course)
            }

            currentLine++
            if(currentLine >= list.size)
                shouldRead = false
        }

        return courses
    }

    interface DownloadListener {
        fun onDownloadFinished()
    }
}