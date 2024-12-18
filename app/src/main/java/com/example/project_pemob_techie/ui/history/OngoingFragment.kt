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
import com.example.project_pemob_techie.databinding.FragmentOngoingBinding
import com.google.firebase.database.*

class OngoingFragment : Fragment() {

    private var _binding: FragmentOngoingBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    private val ongoingTransactions = mutableListOf<String>()
    private lateinit var adapter: OngoingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOngoingBinding.inflate(inflater, container, false)

        userId = arguments?.getString("USER_ID")
        if (userId.isNullOrEmpty()) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        setupRecyclerView()
        

        val databaseRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("6/transaction/$userId")

        fetchTransactionIds(databaseRef)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        
        val databaseRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("6/transaction/$userId")

        fetchTransactionIds(databaseRef)
    }


    private fun setupRecyclerView() {
        adapter = OngoingAdapter(ongoingTransactions) { transactionId ->
            val intent = Intent(requireContext(), OngoingActivity::class.java)
            intent.putExtra("TRANSACTION_ID", transactionId)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        binding.recyclerOngoing.layoutManager = LinearLayoutManager(context)
        binding.recyclerOngoing.adapter = adapter
    }


    private fun fetchTransactionIds(databaseRef: DatabaseReference) {
        showLoading(true)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ongoingTransactions.clear()
                for (transaction in snapshot.children) {
                    val transactionId = transaction.key
                    val shippingStatus = transaction.child("shipping_status").getValue(Boolean::class.java) ?: true
                    if (!shippingStatus) {
                        transactionId?.let { ongoingTransactions.add(it) }
                    }
                }
                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Toast.makeText(context, "Failed to fetch data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (!isLoading) {
            updateUI()
        }
    }

    private fun updateUI() {
        if (ongoingTransactions.isEmpty()) {
            binding.textOngoingEmpty.visibility = View.VISIBLE
            binding.recyclerOngoing.visibility = View.GONE
        } else {
            binding.textOngoingEmpty.visibility = View.GONE
            binding.recyclerOngoing.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
