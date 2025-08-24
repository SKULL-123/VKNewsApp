package com.example.vknewsapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.net.toUri

class LoginActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefs = getSharedPreferences("vk_prefs", MODE_PRIVATE)

        val accessToken = prefs.getString("access_token", null)
        if (accessToken != null) {
            startActivity(Intent(this, NewsFeedActivity::class.java))
            finish()
            return
        }

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            openWebViewForAuth()
        }
    }

    private fun openWebViewForAuth() {
        val intent = Intent(this, WebAuthActivity::class.java)
        startActivity(intent)
    }
}