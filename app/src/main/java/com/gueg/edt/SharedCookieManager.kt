package com.gueg.edt


import java.net.CookieManager
import java.net.URI
import java.util.*

class SharedCookieManager(cookieManager: android.webkit.CookieManager?) : CookieManager() {

    private var webkitCookieManager: android.webkit.CookieManager? = null

    override fun put(uri: URI, responseHeaders: Map<String?, List<String?>>?) {
        if (responseHeaders == null) {
            return
        }
        for (headerKey in responseHeaders.keys) {
            if (headerKey == null || !"Set-Cookie".equals(headerKey, ignoreCase = true)) {
                continue
            }
            for (headerValue in responseHeaders[headerKey]!!) {
                webkitCookieManager!!.setCookie(uri.toString(), headerValue)
            }
        }
    }

    override operator fun get(uri: URI, requestHeaders: Map<String?, List<String?>?>?): Map<String, List<String>>? {
        if (requestHeaders == null) {
            return null
        }
        val res: MutableMap<String, List<String>> = HashMap()

        // get cookies from Webview CookieManager
        var cookie = webkitCookieManager!!.getCookie(uri.toString())
        if (cookie == null) {
            cookie = ""
        }
        res["Cookie"] = listOf(cookie)
        return res
    }

    init {
        webkitCookieManager = cookieManager
        //Cookies are allowed
        webkitCookieManager!!.setAcceptCookie(true)
    }
}