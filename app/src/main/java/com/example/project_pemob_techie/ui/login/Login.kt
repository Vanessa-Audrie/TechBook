package com.example.project_pemob_techie.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.ActivityLoginBinding
import com.example.project_pemob_techie.MainActivity
import com.example.project_pemob_techie.ui.account.SessionManager
import com.example.project_pemob_techie.ui.content.Home
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isLoggedIn = SessionManager.isLoggedIn(this)

        if (isLoggedIn) {
            startActivity(Intent(this, Home::class.java))
            finish()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, VerifyEmail::class.java))
        }

        binding.loginButton.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                verifyUserCredentials(username, password)
            } else if (username.isEmpty()) {
                binding.usernameInput.error = "Username is required"
            }
            if (password.isEmpty()) {
                binding.passwordInput.error = "Password is required"
            }

        }
    }

    private fun verifyUserCredentials(username: String, password: String) {
        val database = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/").reference
        val userRef = database.child("techbook_techie").child("user")

        userRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userSnapshot = snapshot.children.first()
                    val storedPassword = userSnapshot.child("password").value.toString()

                    if (BCrypt.verifyer().verify(password.toCharArray(), storedPassword.toCharArray()).verified) {

                        startSession(userSnapshot.key.toString())
                        Toast.makeText(this@Login, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Login, Home::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@Login, "Incorrect password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Login, "User not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startSession(userId: String) {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_id", userId)
        editor.putBoolean("is_logged_in", true)
        editor.apply()
        SessionManager.startSession(this, userId)

    }


}
