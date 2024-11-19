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
        val imageViewModel = ViewModelProvider(this).get(BookViewModel::class.java)


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
                bookImageView.setImageResource(R.drawable.error) // Fallback image if file doesn't exist
            }
        }

        val tabLayout: TabLayout = findViewById(R.id.tabbedLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val adapter = BookDetailsPagerAdapter(this, bookSynopsis ?: "No synopsis")
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