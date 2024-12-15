package com.example.project_pemob_techie.ui.cart
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class CartViewModel : ViewModel() {
    private val cartRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("3/cart/userId/")
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    fun loadCartItems(userId: String) {
        cartRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = mutableListOf<CartItem>()
                for (itemSnapshot in snapshot.children) {
                    val bookTitle = itemSnapshot.child("title").value as? String
                    val price = itemSnapshot.child("price").value as? String
                    val image = itemSnapshot.child("image").value as? String
                    val quantity = itemSnapshot.child("quantity").value as? Int ?: 1

                    if (bookTitle != null && price != null && image != null) {
                        cartItems.add(CartItem(itemSnapshot.key ?: "", bookTitle, price, quantity, image))
                    }
                }
                _cartItems.postValue(cartItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartViewModel", "Failed to load cart", error.toException())
            }
        })
    }
}

