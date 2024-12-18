package com.example.project_pemob_techie.ui.wishlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.FragmentWishlistBinding
import com.example.project_pemob_techie.ui.account.SessionManager
import com.example.project_pemob_techie.ui.cart.CartActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WishlistFragment : Fragment(R.layout.fragment_wishlist) {
    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var userId: String
    private var wishlistListener: ValueEventListener? = null
    private var wishlistRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("9/wishlist/userId")

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
        setupUI()
        showLoading(true)
        loadWishlist()

        return binding.root
    }

    private fun setupUI() {
        val cartIcon: ImageView = binding.root.findViewById(R.id.cartWishlist)
        cartIcon.setOnClickListener {
            val intent = Intent(requireContext(), CartActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadWishlist() {
        val userWishlistRef = wishlistRef.child(userId)
        wishlistListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return
                val wishlistItems = mutableListOf<WishlistItem>()
                Log.d("WishlistFragment", "Snapshot data: $snapshot")

                for (itemSnapshot in snapshot.children) {
                    val bookTitle = itemSnapshot.child("title").value as? String
                    val price = itemSnapshot.child("price").value as? String
                    val image = itemSnapshot.child("image").value as? String

                    if (bookTitle != null && price != null && image != null) {
                        wishlistItems.add(WishlistItem(bookTitle, image, price))
                    } else {
                        Log.e("WishlistFragment", "Invalid wishlist item data: $itemSnapshot")
                    }
                }

                updateUI(wishlistItems)
                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                showLoading(false)
                Toast.makeText(context, "Failed to load wishlist: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("WishlistFragment", "Error loading wishlist: ${error.message}")
            }
        }
        userWishlistRef.addValueEventListener(wishlistListener!!)
    }

    private fun updateUI(wishlistItems: List<WishlistItem>) {
        if (wishlistItems.isEmpty()) {
            binding.tvstatus.visibility = View.VISIBLE
        } else {
            binding.tvstatus.visibility = View.GONE
        }
        setupRecyclerView(wishlistItems)
    }

    private fun setupRecyclerView(wishlistItems: List<WishlistItem>) {
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = WishlistAdapter(wishlistItems.toMutableList()) { item ->
            removeItemFromWishlist(item)
        }
    }

    private fun removeItemFromWishlist(item: WishlistItem) {
        val userWishlistRef = wishlistRef.child(userId)
        userWishlistRef.orderByChild("title").equalTo(item.bookTitle).limitToFirst(1).get()
            .addOnSuccessListener { snapshot ->
                for (itemSnapshot in snapshot.children) {
                    itemSnapshot.ref.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Item removed from wishlist.", Toast.LENGTH_SHORT).show()
                            loadWishlist()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to remove item: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("WishlistFragment", "Error removing item: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error finding item to remove: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("WishlistFragment", "Error finding item to remove: ${e.message}")
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firebase listener to avoid memory leaks
        wishlistListener?.let {
            wishlistRef.child(userId).removeEventListener(it)
        }
        _binding = null
    }
}
