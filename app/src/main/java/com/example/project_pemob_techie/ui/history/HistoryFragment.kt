package com.example.project_pemob_techie.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.project_pemob_techie.databinding.FragmentHistoryBinding
import com.example.project_pemob_techie.ui.account.SessionManager
import com.google.android.material.tabs.TabLayoutMediator

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        userId = SessionManager.getUserId(requireContext()) ?: run {
            throw IllegalStateException("User not logged in")
        }

        val adapter = HistoryPagerAdapter(this, userId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Ongoing"
                1 -> tab.text = "Completed"
            }
        }.attach()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HistoryPagerAdapter(fragment: Fragment, private val userId: String) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> OngoingFragment()
            else -> CompletedFragment()
        }

        fragment.arguments = Bundle().apply {
            putString("USER_ID", userId)
        }
        return fragment
    }
}
