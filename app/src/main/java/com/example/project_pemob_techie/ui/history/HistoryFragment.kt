package com.example.project_pemob_techie.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project_pemob_techie.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.namaBuku.text = "Path of the Dragon"
        binding.tvOrderStatus.text = "Package delivered to Techie Soekarnoputri"
        binding.tvAddress.text = """
            Techie Soekarnoputri
            +62 823 1234 5678
            Banteng Road No 3, Medan Selayang
            North Sumatra, 20389
        """.trimIndent()

        binding.btnSendReview.setOnClickListener {
            val reviewText = binding.etReview.text.toString()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
