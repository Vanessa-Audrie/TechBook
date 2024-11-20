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
        val bookImagePath = intent.getStringExtra("BOOK_IMG_PATH")
        val bookSynopsis = intent.getStringExtra("BOOK_SYNOPSIS")
        val bookISBN = intent.getStringExtra("BOOK_ISBN")
        val bookAuthor = intent.getStringExtra("BOOK_AUTHOR")
        val bookLanguage = intent.getStringExtra("BOOK_LANG")
        val bookPages = intent.getStringExtra("BOOK_PAGES")
        val bookDate = intent.getStringExtra("BOOK_DATE")
        val bookMass = intent.getStringExtra("BOOK_MASS")
        val bookPublisher = intent.getStringExtra("BOOK_PUBLISHER")

        bookTitleTextView.text = bookTitle
        bookPriceTextView.text = "Rp $bookPrice"

        Log.d("BookDetails", "Received synopsis: $bookSynopsis")

        if (!bookImagePath.isNullOrEmpty()) {
            val file = File(bookImagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bookImageView.setImageBitmap(bitmap)
            } else {
                bookImageView.setImageResource(R.drawable.error)
            }
        }

        val tabLayout: TabLayout = findViewById(R.id.tabbedLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val bookDetails = mapOf(
            "ISBN" to (bookISBN ?: "N/A"),
            "Author" to (bookAuthor ?: "N/A"),
            "Language" to (bookLanguage ?: "N/A"),
            "Pages" to (bookPages ?: "N/A"),
            "Date" to (bookDate ?: "N/A"),
            "Mass" to (bookMass ?: "N/A"),
            "Publisher" to (bookPublisher ?: "N/A")
        )

        val adapter = BookDetailsPagerAdapter(this, bookSynopsis ?: "No synopsis", bookDetails)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Description"
                1 -> tab.text = "Details"
                2 -> tab.text = "Rating & Review"
            }
        }.attach()
    }





}