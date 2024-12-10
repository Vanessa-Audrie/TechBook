package com.example.project_pemob_techie.ui.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DataAccessLocal {
    @Insert
    suspend fun insert(cartItem: CartItemEntity)

    @Query("SELECT * FROM cart_items WHERE selected = 1")
    suspend fun getSelectedItems(): List<CartItemEntity>

    @Query("DELETE FROM cart_items")
    suspend fun deleteAllItems()
}
