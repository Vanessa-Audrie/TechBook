package com.example.project_pemob_techie.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.FragmentCompletedBinding
import com.google.firebase.database.*

class CompletedFragment : Fragment() {

    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    private val completedTransactions = mutableListOf<String>()
    private lateinit var adapter: CompletedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedBinding.inflate(inflater, container, false)

        userId = arguments?.getString("USER_ID")
        if (userId.isNullOrEmpty()) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        setupRecyclerView()
        

        val databaseRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("6/transaction/$userId")

        fetchCompletedTransactions(databaseRef)

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = CompletedAdapter(completedTransactions) { transactionId ->
            val intent = Intent(context, CompletedActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("TRANSACTION_ID", transactionId)
            startActivity(intent)
        }
        binding.recyclerCompleted.layoutManager = LinearLayoutManager(context)
        binding.recyclerCompleted.adapter = adapter
    }

    private fun fetchCompletedTransactions(databaseRef: DatabaseReference) {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                completedTransactions.clear()
                for (transaction in snapshot.children) {
                    val transactionId = transaction.key
                    val shippingStatus = transaction.child("shipping_status").getValue(Boolean::class.java) ?: false

                    if (shippingStatus) {
                        transactionId?.let { completedTransactions.add(it) }
                    }
                }

                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateUI() {
        if (completedTransactions.isEmpty()) {
            binding.textCompletedEmpty.visibility = View.VISIBLE
            binding.recyclerCompleted.visibility = View.GONE
        } else {
            binding.textCompletedEmpty.visibility = View.GONE
            binding.recyclerCompleted.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
