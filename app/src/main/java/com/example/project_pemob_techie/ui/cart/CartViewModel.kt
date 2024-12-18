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

                    val quantityRaw = itemSnapshot.child("quantity").value
                    val quantity = when (quantityRaw) {
                        is Long -> quantityRaw.toInt()
                        is String -> quantityRaw.toIntOrNull() ?: 0
                        is Int -> quantityRaw
                        else -> 0
                    }
                    val massRaw = itemSnapshot.child("mass").value
                    val mass = when (massRaw) {
                        is Long -> massRaw.toDouble()
                        is Double -> massRaw
                        is String -> massRaw.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    if (bookTitle != null && price != null && image != null && quantity > 0 && mass != null) {
                        cartItems.add(CartItem(itemSnapshot.key ?: "", bookTitle, price, quantity, image, mass))
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

