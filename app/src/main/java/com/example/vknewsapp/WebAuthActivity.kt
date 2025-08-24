package com.example.vknewsapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri

class WebAuthActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_auth)

        prefs = getSharedPreferences("vk_prefs", MODE_PRIVATE)
        webView = findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && url.contains("access_token")) {
                    val token = extractTokenFromUrl(url)
                    if (token != null) {
                        saveAccessToken(token)
                        startActivity(Intent(this@WebAuthActivity, NewsFeedActivity::class.java))
                        finish()
                        return true
                    }
                }
                return false
            }
        }

        // URL для авторизации VK
        val authUrl = "https://oauth.vk.com/authorize?" +
                "client_id=54071752&" +
                "display=mobile&" +
                "redirect_uri=https://oauth.vk.com/blank.html&" +
                "scope=wall,photos,groups&" +
                "response_type=token&" +
                "v=5.131"

        webView.loadUrl(authUrl)
    }

    private fun extractTokenFromUrl(url: String): String? {
        try {
            val uri = url.toUri()
            val fragment = uri.fragment
            if (fragment != null) {
                val params = fragment.split("&")
                for (param in params) {
                    if (param.startsWith("access_token=")) {
                        return param.substring("access_token=".length)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun saveAccessToken(token: String) {
        prefs.edit().putString("access_token", token).apply()
    }
}