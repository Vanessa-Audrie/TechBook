package com.example.project_pemob_techie.ui.genre

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R

class GenreAdapter(
    private val genreList: List<String>,
    private val onGenreClick: (String) -> Unit // Callback untuk klik genre
    ) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    // ViewHolder untuk setiap item genre
    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val genreTextView: TextView = itemView.findViewById(R.id.textView58)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_genre, parent, false)
        return GenreViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genreList[position]
        holder.genreTextView.text = genreList[position]
        holder.genreTextView.text = genre

        holder.itemView.setOnClickListener {
            onGenreClick(genre)
        }
    }

    override fun getItemCount(): Int = genreList.size
}