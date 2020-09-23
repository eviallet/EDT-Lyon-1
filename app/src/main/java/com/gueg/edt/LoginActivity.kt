package com.gueg.edt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import org.threeten.bp.LocalDate


class LoginActivity : Activity() {

    companion object {
        const val ADE_URL_EXTRA = "ADE_URL"
    }

    private lateinit var webView: WebView

    private val URL_LOGIN = "https://cas.univ-lyon1.fr/cas/login?service=https%3A%2F%2Fwww.univ-lyon1.fr%2Fservlet%2Fcom.jsbsoft.jtf.core.SG%3FPROC%3DIDENTIFICATION_FRONT"
    private val URL_HOMEPAGE = "https://www.univ-lyon1.fr"
    private val URL_SCHEDULE = "https://edt.univ-lyon1.fr"
    private val URL_END_PAGE = "https://sciences-licence.univ-lyon1.fr/"

    private val DEFAULT_ADE_URL = "https://adelb.univ-lyon1.fr/jsp/custom/modules/plannings/anonymous_cal.jsp?resources=RESOURCES_NUMBER&projectId=2&calType=ical&firstDate=FIRST_DATE&lastDate=LAST_DATE"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true

        setContentView(webView)

        webView.webViewClient = CustomClient()

        webView.loadUrl(URL_LOGIN)
    }

    inner class CustomClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            if(url == null)
                return

            if(url.startsWith(URL_HOMEPAGE)) {
                Log.d(":-:", "%%% onLoginFinished")
                webView.loadUrl(URL_SCHEDULE)
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            Log.d(":-:", "%%% Page finished : $url")

            if(url == null)
                return

            if(url.startsWith(URL_END_PAGE)) {
                webView.evaluateJavascript(
                    "(function() { " +
                            "return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); " +
                    "})();"
                ) { html ->
                    decodeHTML(html)
                }
            }
        }
    }


    fun decodeHTML(html: String) {
        // isolation de la partie HTML contenant l'URL ADE
        var cut = html.substringAfter("Emploi du temps Complet")
        cut = cut.substringAfter("href=\\\"")
        cut = cut.substringBefore("\"")

        // isolation des numéros de ressource
        cut = cut.substringAfter("resources=")
        cut = cut.substringBefore("&")

        Log.d(":-:", "=== Resources : $cut")

        // construction de l'URL ADE
        val nowDate = LocalDate.now()
        val startDate = nowDate.minusMonths(2)
        val lastDate = nowDate.plusMonths(8)

        val url = DEFAULT_ADE_URL
            .replace("RESOURCES_NUMBER", cut)
            .replace("FIRST_DATE", startDate.toString())
            .replace("LAST_DATE", lastDate.toString())

        Log.d(":-:", "=== URL final : $url")

        val intent = Intent()
        intent.putExtra(ADE_URL_EXTRA, url)
        setResult(RESULT_OK, intent)
        finish()
    }

}