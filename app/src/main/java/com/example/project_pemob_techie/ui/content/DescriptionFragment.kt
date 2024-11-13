    package com.example.project_pemob_techie.ui.content


    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.fragment.app.Fragment
    import com.example.project_pemob_techie.R
    import android.widget.TextView

    class DescriptionFragment : Fragment(R.layout.fragment_description) {

        companion object {
            private const val ARG_SYNOPSIS = "synopsis"

            // Factory method to create a new instance of this fragment
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

            // Retrieve the synopsis from arguments
            synopsis = arguments?.getString(ARG_SYNOPSIS)

            // Display the synopsis in a TextView or however you'd like
            val synopsisTextView: TextView = view.findViewById(R.id.synopsisTextView)
            synopsisTextView.text = synopsis
        }
    }
