package com.example.project_pemob_techie.ui.account

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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
    private lateinit var profileImageView: ImageView
    private lateinit var textView96: TextView
    private lateinit var  EditImageView: ImageView

    private var selectedImageBase64: String? = null

    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_profile)

        database = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("techbook_techie/")

        emailTextView = findViewById(R.id.textView107)
        birthdayTextView = findViewById(R.id.textView110)
        languageTextView = findViewById(R.id.textView111)
        nameEditText = findViewById(R.id.textView105)
        phoneNumberEditText = findViewById(R.id.textView108)
        usernameEditText = findViewById(R.id.textView106)
        saveButton = findViewById(R.id.button12)
        profileImageView = findViewById(R.id.imageView35)
        textView96 = findViewById(R.id.textView96)
        EditImageView = findViewById(R.id.imageView37)

        fetchUserData()

        saveButton.setOnClickListener {
            updateUserProfile()
        }

        val imageView36 = findViewById<ImageView>(R.id.imageView36)
        imageView36.setOnClickListener {
            finish()
        }

        textView96.setOnClickListener {
            openImagePicker()
        }

        EditImageView.setOnClickListener {
            openImagePicker()
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
                    val profileImageBase64 = snapshot.child("profileImageUrl").value.toString()

                    emailTextView.text = email
                    birthdayTextView.text = birthday
                    languageTextView.text = "English"

                    nameEditText.setText(name)
                    phoneNumberEditText.setText(phoneNumber)
                    usernameEditText.setText(username)

                    if (profileImageBase64.isNotEmpty()) {
                        val decodedImage = decodeBase64ToBitmap(profileImageBase64)
                        profileImageView.setImageBitmap(decodedImage)
                    } else {
                        profileImageView.setImageResource(R.drawable.profile)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun updateUserProfile() {
        val updatedName = nameEditText.text.toString()
        val updatedPhoneNumber = phoneNumberEditText.text.toString()
        val updatedUsername = usernameEditText.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val userRef = database.child("user").child(userId)
            val updates = mutableMapOf<String, Any>(
                "name" to updatedName,
                "phone" to updatedPhoneNumber,
                "username" to updatedUsername
            )

            if (selectedImageBase64 != null) {
                updates["profileImageUrl"] = selectedImageBase64!!
            }

            saveUpdates(userRef, updates)
        }
    }

    private fun saveUpdates(userRef: DatabaseReference, updates: Map<String, Any>) {
        userRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun convertImageToBase64(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val byteArray = inputStream?.readBytes()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            selectedImageUri?.let {
                selectedImageBase64 = convertImageToBase64(it)

                profileImageView.setImageURI(it)
            }
        }
    }
}
