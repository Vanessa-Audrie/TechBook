package com.example.project_pemob_techie.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_pemob_techie.MainActivity
import com.example.project_pemob_techie.databinding.ActivityLoginSignupBinding
import com.example.project_pemob_techie.ui.account.SessionManager

class LoginSignup : AppCompatActivity() {
    private lateinit var binding: ActivityLoginSignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        binding = ActivityLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, Signup::class.java))
        }
    }
}
