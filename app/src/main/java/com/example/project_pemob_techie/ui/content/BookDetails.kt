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
import com.example.project_pemob_techie.ui.cart.CartViewModel
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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



        backButton.setOnClickListener {
            finish()
        }
        cartIcon.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }


        val bookTitle = intent.getStringExtra("BOOK_TITLE")
        val bookPrice = intent.getStringExtra("BOOK_PRICE")
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
        bookPriceTextView.text = "Rp $bookPrice"

        if (!bookImagePath.isNullOrEmpty()) {
            val file = File(bookImagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bookImageView.setImageBitmap(bitmap)
            } else {
                bookImageView.setImageResource(R.drawable.error)
            }
        }


        addToCartButton.setOnClickListener {
            val userId = SessionManager.getUserId(this)
            if (userId.isNullOrEmpty() || bookISBN.isNullOrEmpty()) {
                Toast.makeText(this, "User not logged in or Invalid book ID", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Ambil data buku yang ingin ditambahkan ke keranjang
            val cartItem = com.example.project_pemob_techie.ui.cart.CartItem(
                bookId = bookISBN ?: "", // Gunakan elvis operator jika bookISBN mungkin null
                book_title = bookTitle ?: "Unknown Title", // Default "Unknown Title" jika null
                price = bookPrice ?: "0", // Mengonversi harga menjadi integer
                quantity = 1 // Misalnya, menetapkan quantity 1
            )

            viewModel.addToCart(cartItem)





            // Tambahkan item ke Firebase
            val cartRef = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/").getReference("3/cart/userId/$userId")
            cartRef.push().setValue(cartItem).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Item added to cart!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add item to cart.", Toast.LENGTH_SHORT).show()
                }
            }
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
            FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/")
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
    data class CartItem(
        val bookId: String = "",
        val bookTitle: String = "",
        val bookPrice: Int = 0,
        val quantity: Int = 1
    )



}
