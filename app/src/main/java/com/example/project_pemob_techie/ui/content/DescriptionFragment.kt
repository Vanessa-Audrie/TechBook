package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.project_pemob_techie.R

class DescriptionFragment : Fragment() {

    companion object {
        private const val ARG_SYNOPSIS = "synopsis"

        fun newInstance(synopsis: String): DescriptionFragment {
            val fragment = DescriptionFragment()
            val args = Bundle()
            args.putString(ARG_SYNOPSIS, synopsis)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_description, container, false)

        // Retrieve the synopsis argument and set it to synopsisTextView
        val synopsis = arguments?.getString(ARG_SYNOPSIS)
        view.findViewById<TextView>(R.id.synopsisTextView).text = synopsis

        return view
    }
}

