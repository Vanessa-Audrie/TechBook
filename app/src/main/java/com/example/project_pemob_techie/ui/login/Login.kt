package com.example.project_pemob_techie.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.ActivityLoginBinding
import com.example.project_pemob_techie.MainActivity

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            finish() // Go back to the previous activity
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, VerifyEmail::class.java))
        }

        // Handle Login Button
        binding.loginButton.setOnClickListener {
            // Navigate to MainActivity (HomeFragment)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
