package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.project_pemob_techie.R
import android.widget.TextView

class DescriptionFragment : Fragment(R.layout.fragment_description) {

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

    private var synopsis: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        synopsis = arguments?.getString(ARG_SYNOPSIS)
        Log.d("DescriptionFragment", "Received synopsis: $synopsis")

        if (synopsis.isNullOrEmpty()) {
            synopsis = "No synopsis available."
        }

        val synopsisTextView: TextView = view.findViewById(R.id.synopsisTextView)
        synopsisTextView.text = synopsis
    }
}
