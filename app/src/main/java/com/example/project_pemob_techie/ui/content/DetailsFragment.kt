package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.project_pemob_techie.R

class DetailsFragment : Fragment() {

    private var bookDetails: Map<String, String>? = null

    companion object {
        private const val BOOK_DETAILS_KEY = "BOOK_DETAILS_KEY"

        fun newInstance(details: Map<String, String>): DetailsFragment {
            val fragment = DetailsFragment()
            val bundle = Bundle()
            bundle.putSerializable(BOOK_DETAILS_KEY, HashMap(details))
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookDetails = arguments?.getSerializable(BOOK_DETAILS_KEY) as? Map<String, String>
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)

        bookDetails?.let { details ->
            view.findViewById<TextView>(R.id.bookISBNValue)?.text = details["ISBN"]
            view.findViewById<TextView>(R.id.bookAuthorValue)?.text = details["Author"]
            view.findViewById<TextView>(R.id.bookLanguageValue)?.text = details["Language"]
            view.findViewById<TextView>(R.id.bookPagesValue)?.text = details["Pages"]
            view.findViewById<TextView>(R.id.bookDateValue)?.text = details["Date"]
            view.findViewById<TextView>(R.id.bookMassValue)?.text = details["Mass"]
            view.findViewById<TextView>(R.id.bookPublisherValue)?.text = details["Publisher"]
        }

        return view
    }
}

