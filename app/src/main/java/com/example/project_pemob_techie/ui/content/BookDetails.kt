package com.example.project_pemob_techie.ui.content

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import coil.load
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
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.project_pemob_techie.ui.cart.CartActivity
import com.example.project_pemob_techie.ui.cart.CartItem
import com.example.project_pemob_techie.ui.cart.CartViewModel
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale

class BookDetails : AppCompatActivity() {
    private lateinit var viewModel: CartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_page)

        viewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        val backButton: ImageView = findViewById(R.id.imageView29)
        val bookTitleTextView: TextView = findViewById(R.id.textView85)
        val bookPriceTextView: TextView = findViewById(R.id.textView86)
        val bookImageView: ImageView = findViewById(R.id.bookImage)
        val cartIcon: ImageView = findViewById(R.id.imageView32)
        val addToCartButton: Button = findViewById(R.id.button10)
        val purchaseCountTextView: TextView = findViewById(R.id.textView87)

        backButton.setOnClickListener {
            finish()
        }

        cartIcon.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val bookTitle = intent.getStringExtra("BOOK_TITLE")
        val bookImagePath = intent.getStringExtra("BOOK_IMG_PATH")
        val bookSynopsis = intent.getStringExtra("BOOK_SYNOPSIS")
        val bookISBN = intent.getStringExtra("BOOK_ISBN")
        val bookAuthor = intent.getStringExtra("BOOK_AUTHOR")
        val bookLanguage = intent.getStringExtra("BOOK_LANG")
        val bookPages = intent.getStringExtra("BOOK_PAGES")
        val bookDate = intent.getStringExtra("BOOK_DATE")
        val bookMass = intent.getStringExtra("BOOK_MASS")
        val bookPublisher = intent.getStringExtra("BOOK_PUBLISHER")
        bookTitleTextView.text = bookTitle ?: "No Title Available"
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        val bookPrice = intent.getStringExtra("BOOK_PRICE")?.toDoubleOrNull()
        bookPriceTextView.text = "Rp ${formatter.format(bookPrice)}"

        if (!bookImagePath.isNullOrEmpty()) {
            val file = File(bookImagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bookImageView.setImageBitmap(bitmap)
            } else {
                bookImageView.setImageResource(R.drawable.error)
            }
        }

        if (!bookISBN.isNullOrEmpty()) {
            fetchPurchaseCount(bookISBN, purchaseCountTextView)
        }

        val bookDetails = mapOf(
            "ISBN" to (bookISBN ?: "N/A"),
            "Author" to (bookAuthor ?: "N/A"),
            "Language" to (bookLanguage ?: "N/A"),
            "Pages" to (bookPages ?: "N/A"),
            "Date" to (bookDate ?: "N/A"),
            "Mass" to (bookMass ?: "N/A"),
            "Publisher" to (bookPublisher ?: "N/A")
        )

        val tabLayout: TabLayout = findViewById(R.id.tabbedLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val adapter = BookDetailsPagerAdapter(this, bookSynopsis ?: "No synopsis", bookDetails)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Description"
                1 -> tab.text = "Details"
                2 -> tab.text = "Rating & Review"
            }
        }.attach()

        val database =
            FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("9/")
        val wishlistRef = database.child("wishlist").child("userId")
        val itemId = bookISBN ?: ""
        val wishlistButton = findViewById<ImageView>(R.id.imageView38)

        val userId = SessionManager.getUserId(this)
        if (userId.isNullOrEmpty()) {
            return
        }
        val userWishlistRef = wishlistRef.child(userId).child(itemId)

        userWishlistRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isWishlisted = snapshot.exists()
                if (isWishlisted) {
                    wishlistButton.setImageResource(R.drawable.wishlisted)
                    wishlistButton.tag = true
                } else {
                    wishlistButton.setImageResource(R.drawable.wishlist)
                    wishlistButton.tag = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BookDetails, "Failed to check wishlist", Toast.LENGTH_SHORT).show()
            }
        })

        wishlistButton.setOnClickListener {
            if (itemId.isEmpty()) {
                Toast.makeText(this, "Invalid book ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SessionManager.getUserId(this)
            if (userId.isNullOrEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userWishlistRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isWishlisted = snapshot.exists()

                    if (isWishlisted) {
                        userWishlistRef.removeValue().addOnSuccessListener {
                            wishlistButton.setImageResource(R.drawable.wishlist)
                            wishlistButton.tag = false
                            Toast.makeText(this@BookDetails, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this@BookDetails, "Failed to update wishlist", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val hexString = getHexStringFromImagePath(bookImagePath)
                        val wishlistItem = mapOf(
                            "isbn" to itemId,
                            "title" to bookTitle,
                            "price" to bookPrice,
                            "image" to hexString
                        )
                        userWishlistRef.setValue(wishlistItem).addOnSuccessListener {
                            wishlistButton.setImageResource(R.drawable.wishlisted)
                            wishlistButton.tag = true
                            Toast.makeText(this@BookDetails, "Added to wishlist", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this@BookDetails, "Failed to update wishlist", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@BookDetails, "Failed to check wishlist", Toast.LENGTH_SHORT).show()
                }
            })
        }

        val databasecart =
            FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("3/")
        val cartRef = databasecart.child("cart").child("userId")
        val cartitemId = bookISBN ?: ""

        if (userId.isNullOrEmpty()) {
            return
        }

        val userCartRef = cartRef.child(userId).child(cartitemId)

        addToCartButton.setOnClickListener {
            if (itemId.isEmpty()) {
                Toast.makeText(this, "Invalid book ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SessionManager.getUserId(this)
            if (userId.isNullOrEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hexString = getHexStringFromImagePath(bookImagePath)
            val cartItem = mapOf(
                "isbn" to itemId,
                "title" to bookTitle,
                "price" to bookPrice,
                "image" to hexString,
                "quantity" to 1
            )

            userCartRef.setValue(cartItem).addOnSuccessListener {
                Toast.makeText(this@BookDetails, "Added to cart", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this@BookDetails, "Failed to update cart", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun fetchPurchaseCount(isbn: String, purchaseCountTextView: TextView) {
        val database = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("7/transaction_details")

        var totalQuantity = 0

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (randomNode in snapshot.children) {
                    for (subNode in randomNode.children) {
                        val transactionIsbn = subNode.child("isbn").getValue(String::class.java)
                        if (transactionIsbn == isbn) {
                            val quantity = subNode.child("quantity").getValue(Int::class.java) ?: 0
                            totalQuantity += quantity
                        }
                    }
                }
                purchaseCountTextView.text = "($totalQuantity has been bought)"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BookDetails, "Failed to load purchase count", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun getHexStringFromImagePath(imagePath: String?): String {
        if (imagePath.isNullOrEmpty()) return ""
        val file = File(imagePath)
        if (!file.exists()) return ""
        return try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return ""
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val bytes = outputStream.toByteArray()
            val hexString = bytes.joinToString("") { byte -> "%02x".format(byte) }
            bitmap.recycle()
            outputStream.close()
            hexString
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

}
