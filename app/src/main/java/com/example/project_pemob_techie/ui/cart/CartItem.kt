package com.example.project_pemob_techie.ui.cart

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

data class CartItem(
    val bookId: String,
    val bookTitle: String,
    val price: String,
    var quantity: Int,
    val image: String?,
    var selected: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bookId)
        parcel.writeString(bookTitle)
        parcel.writeString(price)
        parcel.writeInt(quantity)
        parcel.writeString(image)
        parcel.writeByte(if (selected) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CartItem> {
        override fun createFromParcel(parcel: Parcel): CartItem = CartItem(parcel)
        override fun newArray(size: Int): Array<CartItem?> = arrayOfNulls(size)
    }
}

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val bookTitle: String,
    val price: String,
    val quantity: Int,
    val image: String?,
    val selected: Boolean
)
