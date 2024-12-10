package com.example.project_pemob_techie.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project_pemob_techie.databinding.FragmentCompletedBinding

class CompletedFragment : Fragment() {

    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCompletedBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



