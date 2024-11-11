package com.example.project_pemob_techie.ui.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R

class RecAdapter(private val recommendations: List<DisplayData>) :
    RecyclerView.Adapter<RecAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.recomTitle)
        val price: TextView = view.findViewById(R.id.price)
        val image: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_recom_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recommendation = recommendations[position]
        holder.title.text = recommendation.title
        holder.price.text = "Rp ${recommendation.price}"
        holder.image.setImageBitmap(recommendation.image)  // Set the Bitmap image
    }

    override fun getItemCount(): Int = recommendations.size
}

