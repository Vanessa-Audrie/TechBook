package com.example.project_pemob_techie.ui.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_pemob_techie.MainActivity
import com.example.project_pemob_techie.R

class PaymentDone : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_of_checkout)

        val backButton = findViewById<ImageView>(R.id.imageView20)
        val backToHomeButton = findViewById<Button>(R.id.button7)

        backButton.setOnClickListener {
            navigateToMainActivity()
        }

        backToHomeButton.setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
