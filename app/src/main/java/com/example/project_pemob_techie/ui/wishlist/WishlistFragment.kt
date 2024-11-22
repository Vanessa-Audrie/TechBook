package com.example.project_pemob_techie.ui.wishlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.FragmentWishlistBinding
import com.example.project_pemob_techie.ui.account.SessionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WishlistFragment : Fragment(R.layout.fragment_wishlist) {
    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)

        val isLoggedIn = SessionManager.isLoggedIn(requireContext())
        if (!isLoggedIn) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return binding.root
        }
        userId = SessionManager.getUserId(requireContext()) ?: return binding.root
        loadWishlist()
        return binding.root
    }

    private fun loadWishlist() {
        val wishlistRef = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/")
            .getReference("9/wishlist/$userId")

        wishlistRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wishlistItems = mutableListOf<WishlistItem>()

                for (itemSnapshot in snapshot.children) {
                    val bookTitle = itemSnapshot.child("title").value as? String
                    val price = itemSnapshot.child("price").value as? String
                    val image = itemSnapshot.child("image").value as? String

                    if (bookTitle != null && price != null && image != null) {
                        wishlistItems.add(WishlistItem(bookTitle, image, price))
                    }
                }

                Log.d("WishlistFragment", "Wishlist items: $wishlistItems")
                setupRecyclerView(wishlistItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load wishlist: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView(wishlistItems: List<WishlistItem>) {
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = WishlistAdapter(wishlistItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
