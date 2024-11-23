package com.example.project_pemob_techie.ui.account

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.project_pemob_techie.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var emailTextView: TextView
    private lateinit var birthdayTextView: TextView
    private lateinit var languageTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_profile)
        database = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/").getReference("techbook_techie/")

        emailTextView = findViewById(R.id.textView107)
        birthdayTextView = findViewById(R.id.textView110)
        languageTextView = findViewById(R.id.textView111)

        nameEditText = findViewById(R.id.textView105)
        phoneNumberEditText = findViewById(R.id.textView108)
        usernameEditText = findViewById(R.id.textView106)

        saveButton = findViewById(R.id.button12)
        fetchUserData()
        saveButton.setOnClickListener {
            updateUserProfile()
            finish()
        }
        val imageView36 = findViewById<ImageView>(R.id.imageView36)
        imageView36.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = database.child("user").child(userId)
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    val email = snapshot.child("email").value.toString()
                    val phoneNumber = snapshot.child("phone").value.toString()
                    val username = snapshot.child("username").value.toString()
                    val birthday = snapshot.child("birthday").value.toString()
                    val language = "English"

                    emailTextView.text = email
                    birthdayTextView.text = birthday
                    languageTextView.text = language

                    nameEditText.setText(name)
                    phoneNumberEditText.setText(phoneNumber)
                    usernameEditText.setText(username)
                }
            }
        }
    }

    private fun updateUserProfile() {
        val updatedName = nameEditText.text.toString()
        val updatedPhoneNumber = phoneNumberEditText.text.toString()
        val updatedUsername = usernameEditText.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = database.child("user").child(userId)
            val updates = mapOf<String, Any>(
                "name" to updatedName,
                "phone" to updatedPhoneNumber,
                "username" to updatedUsername
            )
            userRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@EditProfileActivity,"Data saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@EditProfileActivity,"Failed saving data.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
