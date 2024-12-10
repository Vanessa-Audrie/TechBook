package com.example.project_pemob_techie.ui.cart

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CartDao {

    @Insert
    suspend fun insert(cartItem: CartItemEntity)

    @Delete
    suspend fun delete(cartItem: CartItemEntity)

    @Query("SELECT * FROM cart_items")
    suspend fun getAll(): List<CartItemEntity>
}

