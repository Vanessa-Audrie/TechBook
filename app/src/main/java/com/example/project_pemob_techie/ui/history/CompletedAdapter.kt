package com.example.project_pemob_techie.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R

class CompletedAdapter(
    private val transactionList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CompletedAdapter.CompletedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_completed, parent, false)
        return CompletedViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompletedViewHolder, position: Int) {
        val transactionId = transactionList[position]
        holder.bind(transactionId, onItemClick)
    }

    override fun getItemCount(): Int = transactionList.size

    class CompletedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trIdTextView: TextView = itemView.findViewById(R.id.TRid)

        fun bind(transactionId: String, onItemClick: (String) -> Unit) {
            trIdTextView.text = transactionId
            itemView.setOnClickListener { onItemClick(transactionId) }
        }
    }
}

