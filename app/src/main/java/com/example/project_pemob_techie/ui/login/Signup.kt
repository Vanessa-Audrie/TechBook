package com.example.project_pemob_techie.ui.login
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.ActivitySignupBinding
import com.example.project_pemob_techie.ui.login.Login
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/").reference

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.signupButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val username = binding.usernameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val phone = binding.phoneInput.text.toString().trim()
            val birthday = binding.birthdayInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInputs(name, username, email, phone, birthday, password)) {
                val hashedPassword = hashPassword(password)
                saveUserToFirebase(name, username, email, phone, birthday, hashedPassword)
            }
        }
    }

    private fun validateInputs(name: String, username: String, email: String, phone: String, birthday: String, password: String): Boolean {
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty() || birthday.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.length < 10) {
            Toast.makeText(this, "Phone number must be at least 10 digits", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    private fun saveUserToFirebase(name: String, username: String, email: String, phone: String, birthday: String, hashedPassword: String) {
        // Create a user object
        val user = User(name, username, email, phone, birthday, hashedPassword)

        database.child("techbook_techie").child("user").push().setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Login::class.java))
                } else {
                    Toast.makeText(this, "Signup failed. Try again!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

data class User(
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val birthday: String,
    val password: String
)
