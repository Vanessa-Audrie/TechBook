package com.example.project_pemob_techie.ui.content

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import coil.load  // Import Coil
import com.example.project_pemob_techie.R
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineStart
import java.io.File
import android.util.Base64
import android.widget.Toast
import com.example.project_pemob_techie.ui.account.SessionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.graphics.Bitmap

import java.io.ByteArrayOutputStream

class BookDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_page)

        val backButton: ImageView = findViewById(R.id.imageView29)
        val bookTitleTextView: TextView = findViewById(R.id.textView85)
        val bookPriceTextView: TextView = findViewById(R.id.textView86)
        val bookImageView: ImageView = findViewById(R.id.bookImage)

        backButton.setOnClickListener {
            finish()
        }

        val bookTitle = intent.getStringExtra("BOOK_TITLE")
        val bookPrice = intent.getStringExtra("BOOK_PRICE")
        val bookISBN = intent.getStringExtra("BOOK_ISBN")

        bookTitleTextView.text = bookTitle
        bookPriceTextView.text = "Rp $bookPrice"

        if (bookISBN.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid book details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val database = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/")
            .getReference("9/")
        val wishlistRef = database.child("wishlist").child("userId")
        val itemId = bookISBN
        val wishlistButton = findViewById<ImageView>(R.id.imageView38)

        wishlistRef.child(itemId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    wishlistButton.setImageResource(R.drawable.wishlisted)
                    wishlistButton.tag = true
                } else {
                    wishlistButton.setImageResource(R.drawable.wishlist)
                    wishlistButton.tag = false
                }

                // Fetch and set the image from Firebase (hex string)
                val imageHex = snapshot.child("image").value as? String
                if (!imageHex.isNullOrEmpty()) {
                    val bitmap = convertHexToBitmap(imageHex)
                    bookImageView.setImageBitmap(bitmap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BookDetails, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        wishlistButton.setOnClickListener {
            val userId = SessionManager.getUserId(this)
            if (userId.isNullOrEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userWishlistRef = wishlistRef.child(userId).child(itemId)
            val isWishlisted = wishlistButton.tag as? Boolean ?: false
            if (isWishlisted) {
                userWishlistRef.removeValue().addOnSuccessListener {
                    wishlistButton.setImageResource(R.drawable.wishlist)
                    wishlistButton.tag = false
                    Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to update wishlist", Toast.LENGTH_SHORT).show()
                }
            } else {
                val wishlistItem = mapOf(
                    "bookISBN" to bookISBN,
                    "image" to "hexImageString", // The hex string you want to store
                    "price" to bookPrice,
                    "added" to true
                )
                userWishlistRef.setValue(wishlistItem).addOnSuccessListener {
                    wishlistButton.setImageResource(R.drawable.wishlisted)
                    wishlistButton.tag = true
                    Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to update wishlist", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Convert the hex string back to Bitmap
    private fun convertHexToBitmap(hexString: String): Bitmap {
        val byteArray = ByteArray(hexString.length / 2)
        for (i in 0 until hexString.length step 2) {
            byteArray[i / 2] = ((hexString[i].digitToInt(16) shl 4) + hexString[i + 1].digitToInt(16)).toByte()
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    // Optional: Compress Bitmap to save space if necessary
    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)  // Adjust quality as needed
        val compressedByteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(compressedByteArray, 0, compressedByteArray.size)
    }
}
