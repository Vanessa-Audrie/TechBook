package com.example.project_pemob_techie.ui.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_pemob_techie.MainActivity
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.login.LoginSignup

class SplashScreen : AppCompatActivity() {

    private val SPLASH_TIMEOUT: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Set padding to respect system bars (status & navigation bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Delay logic to transition to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginSignup::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish() // Ensure SplashScreen is removed from back stack
        }, SPLASH_TIMEOUT)
    }
}
