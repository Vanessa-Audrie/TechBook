package com.example.project_pemob_techie.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R

class OngoingAdapter(private val transactionList: List<String>, private val itemClickListener: (String) -> Unit) :
    RecyclerView.Adapter<OngoingAdapter.OngoingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OngoingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_ongoing, parent, false)
        return OngoingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OngoingViewHolder, position: Int) {
        val transactionId = transactionList[position]
        holder.bind(transactionId)
    }

    override fun getItemCount(): Int = transactionList.size

    inner class OngoingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trIdTextView: TextView = itemView.findViewById(R.id.TRid)

        fun bind(transactionId: String) {
            trIdTextView.text = transactionId
            itemView.setOnClickListener {
                itemClickListener(transactionId)
            }
        }
    }
}
