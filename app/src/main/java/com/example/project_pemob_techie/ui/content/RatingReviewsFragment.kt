package com.example.project_pemob_techie.ui.content

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class RatingReviewsFragment : Fragment() {

    companion object {
        const val BOOK_ISBN_KEY = "BOOK_ISBN_KEY"

        fun newInstance(isbn: String): RatingReviewsFragment {
            val fragment = RatingReviewsFragment()
            val bundle = Bundle()
            bundle.putString(BOOK_ISBN_KEY, isbn)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RatingAdapter
    private lateinit var tvReviewStatus: TextView
    private lateinit var button14: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rating_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        tvReviewStatus = view.findViewById(R.id.tvReviewStatus)
        button14 = view.findViewById(R.id.button14)
        progressBar = view.findViewById(R.id.progressBar)

        val isbn = arguments?.getString(BOOK_ISBN_KEY)
        if (isbn != null) {
            fetchRatingsAndReviews(isbn)
        }

        val buttonAll: View = view.findViewById(R.id.button14)
        buttonAll.setOnClickListener {
            val isbn = arguments?.getString(BOOK_ISBN_KEY)
            if (isbn != null) {
                val intent = Intent(requireContext(), RatingReviewsActivity::class.java)
                intent.putExtra("BOOK_ISBN_KEY", isbn)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "ISBN not available", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    private fun fetchRatingsAndReviews(isbn: String) {
        showLoading(true)
        
        val database = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("5/rating_reviews/$isbn")

        database.get().addOnCompleteListener { task ->
            showLoading(false)

            if (task.isSuccessful) {
                val snapshot = task.result
                if (!snapshot.exists()) {
                    updateVisibility()
                }
            } else {
                Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        }

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allReviews = mutableListOf<RatingAdapter.Review>()
                var totalRating = 0
                var totalReviews = 0

                val userNames = mutableMapOf<String, String>()

                for (userSnapshot in snapshot.children) {
                    for (transactionSnapshot in userSnapshot.children) {
                        val rating = transactionSnapshot.child("rating").getValue(Int::class.java) ?: 0
                        val review = transactionSnapshot.child("review").getValue(String::class.java) ?: "No review"
                        val timestamp = transactionSnapshot.child("timestamp").getValue(Long::class.java) ?: 0
                        val userId = userSnapshot.key ?: "Unknown User"

                        totalRating += rating
                        totalReviews++

                        if (!userNames.containsKey(userId)) {
                            FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference("techbook_techie/user/$userId")
                                .get()
                                .addOnSuccessListener { userSnapshot ->
                                    val userName = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown User"
                                    userNames[userId] = userName

                                    val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                                    allReviews.add(RatingAdapter.Review(userId, rating, review, userNames[userId] ?: "Unknown User", formattedTimestamp))

                                    val firstThreeReviews = if (allReviews.size > 3) allReviews.take(3) else allReviews
                                    adapter = RatingAdapter(firstThreeReviews)
                                    recyclerView.adapter = adapter

                                    if (totalReviews > 0) {
                                        val averageRating = totalRating.toFloat() / totalReviews
                                        val averageTextView: TextView = requireView().findViewById(R.id.textView8)
                                        averageTextView.text = String.format(Locale.getDefault(), "%.1f/5.0", averageRating)
                                    }
                                }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Toast.makeText(requireContext(), "Failed to load ratings and reviews", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateVisibility() {
        tvReviewStatus.visibility = View.VISIBLE
        button14.visibility = View.GONE
    }
}
