package com.gueg.meteo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WeatherView extends WebView {

    private static final String USER_AGENT_STRING = "Mozilla/5.0 (Linux; Android 6.0; HUAWEI VNS-L21 Build/HUAWEIVNS-L21) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36";

    public static final String GOOGLE = "https://www.google.fr/search?q=meteo+$city$";
    public static final String METEOCIEL = "https://www.meteociel.fr/prevville.php?action=getville&ville=$city$&envoyer=ici";

    public enum Advanced {
        J3,
        J10,
        HPH_GFS,
        HPH_AROME
    }

    private String _url;

    @SuppressLint("SetJavaScriptEnabled")
    public WeatherView(Context context, String url, City city) {
        super(context);
        _url = url;

        setWebChromeClient(new WebChromeClient());

        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                _url = url;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.startsWith("https://www.meteociel.fr/")) {
                    evaluateJavascript(
                    "(function() { " +
                            "return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); " +
                            "})();"
                    , new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            decodeHtml(value);
                        }
                    });
                }
                _url = url;
            }
        });
        setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings webSettings = getSettings();
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setUserAgentString(USER_AGENT_STRING);

        searchCity(city);
    }

    @Override
    public void loadUrl(String url) {
        _url = url;
        Log.d(":-:",_url);
        super.loadUrl(url);
    }

    public void advancedMode(Advanced choice) {
        switch (choice) {
            case J3:
                loadUrl(_url.replace(_url.substring(24,_url.indexOf('/',25)),"previsions"));
                break;
            case J10:
                loadUrl(_url.replace(_url.substring(24,_url.indexOf('/',25)),"tendances"));
                break;
            case HPH_GFS:
                loadUrl(_url.replace(_url.substring(24,_url.indexOf('/',25)),"previsions-wrf-1h"));
                break;
            case HPH_AROME:
                loadUrl(_url.replace(_url.substring(24,_url.indexOf('/',25)),"previsions-arome-1h"));
                break;
        }
    }

    public void searchCity(City city) {
        search(city.toString());
    }

    public void search(String city) {
        if(_url.contains("google.fr")) {
            loadUrl(getUrlForCity(GOOGLE, city));
        } else {
            loadUrl(getUrlForCity(METEOCIEL, city));
        }
    }

    public static String getUrlForCity(String base, String city) {
        return base.replace("$city$",city);
    }

    private void decodeHtml(String html) {

    }
}
