package com.gueg.edt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.BufferedInputStream
import java.net.CookieHandler
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection


class LoginScreen : Activity() {

    companion object {
        const val ADE_URL_EXTRA = "ADE_URL"
    }

    private lateinit var webView: WebView

    private val URL_LOGIN = "https://cas.univ-lyon1.fr/cas/login?service=https%3A%2F%2Fwww.univ-lyon1.fr%2Fservlet%2Fcom.jsbsoft.jtf.core.SG%3FPROC%3DIDENTIFICATION_FRONT"
    private val URL_HOMEPAGE = "https://www.univ-lyon1.fr"
    private val URL_SCHEDULE = "https://edt.univ-lyon1.fr"

    private val DEFAULT_ADE_URL = "http://adelb.univ-lyon1.fr/jsp/custom/modules/plannings/anonymous_cal.jsp?resources=RESOURCES_NUMBER&projectId=2&calType=ical&firstDate=FIRST_DATE&lastDate=LAST_DATE"

    private val cookieManager = SharedCookieManager(android.webkit.CookieManager.getInstance())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CookieHandler.setDefault(cookieManager)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true

        setContentView(webView)

        webView.webViewClient = CustomClient(loginListener)

        webView.loadUrl(URL_LOGIN)
    }

    inner class CustomClient(var listener: LoginListener) : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            Log.d(":-:", "%%% Page finished : $url")

            if(url == null)
                return

            if(url.startsWith(URL_HOMEPAGE)) {
                Log.d(":-:", "%%% onLoginFinished")
                //val cookies: String = CookieManager.getInstance().getCookie(url)
                //Thread {
                view!!.loadUrl(URL_SCHEDULE)
                    //listener.onLoginFinished(cookies)
                //}.start()
            } else if(url.startsWith(URL_SCHEDULE)) {
                view!!.evaluateJavascript(
                    "(function() { " +
                            "return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); " +
                    "})();"
                ) { html ->
                    decodeHTML(html)
                }
            }
        }
    }

    private val loginListener = object : LoginListener {
        override fun onLoginFinished(cookies: String) {
            val connection = URL(URL_SCHEDULE).openConnection() as HttpsURLConnection
            connection.setRequestProperty("Cookie", cookies)
            Log.d(":-:", cookies)
            connection.useCaches = false
            connection.connect()

            try {
                Log.d(":-:", "=== Response code : : ${connection.responseCode}")
                if (connection.responseCode == HttpsURLConnection.HTTP_OK) {
                    Log.d(":-:", "=== Opening input stream")
                    val inputStream = BufferedInputStream(connection.inputStream)
                    //decodeStream(inputStream.readBytes())
                }
            } catch (e: Exception) {
                Log.d(":-:", "=== Opening error stream")
                val errorStream = connection.errorStream
                Log.e(":-:", errorStream.readBytes().toString())
            } finally {
                connection.disconnect()
            }
        }
    }

    fun decodeHTML(html: String) {
        // isolation de la partie HTML contenant l'URL ADE
        var cut = html.substringAfter("Emploi du temps Complet")
        cut = cut.substringAfter("<a href=\"")
        cut = cut.substringBefore("\"")

        // isolation des numéros de ressource
        cut = cut.substringAfter("resources=")
        cut = cut.substringBefore("&")

        Log.d(":-:", "=== Resources : $cut")

        // construction de l'URL ADE
        var url = DEFAULT_ADE_URL

        val calendar = Calendar.getInstance()
        val startDate = Date(calendar.get(Calendar.YEAR), Calendar.SEPTEMBER, 1)
        val lastDate = Date(calendar.get(Calendar.YEAR) + 1, Calendar.AUGUST, 31)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        url = url.replace("RESOURCES_NUMBER", cut).replace(
            "FIRST_DATE", formatter.format(
                startDate
            )
        ).replace("LAST_DATE", formatter.format(lastDate))

        Log.d(":-:", "=== URL final : $url")

        val intent = Intent()
        intent.putExtra(ADE_URL_EXTRA, url)
        setResult(RESULT_OK, intent)
        finish()
    }

    interface LoginListener {
        fun onLoginFinished(cookies: String)
    }

}