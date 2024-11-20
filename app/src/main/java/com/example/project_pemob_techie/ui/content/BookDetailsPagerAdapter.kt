package com.example.project_pemob_techie.ui.content

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class BookDetailsPagerAdapter(
    activity: AppCompatActivity,
    private val synopsis: String,
    private val bookDetails: Map<String, String>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DescriptionFragment.newInstance(synopsis)
            1 -> DetailsFragment.newInstance(bookDetails)
            2 -> RatingReviewsFragment()
            else -> DescriptionFragment.newInstance(synopsis)
        }
    }
}


