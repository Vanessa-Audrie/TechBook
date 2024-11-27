package com.example.project_pemob_techie.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.FragmentHomeBinding
import com.example.project_pemob_techie.ui.cart.CartActivity
import com.example.project_pemob_techie.ui.content.BookResponse
import com.example.project_pemob_techie.ui.content.RecAdapter
import com.example.project_pemob_techie.ui.content.SearchResultActivity
import com.example.project_pemob_techie.ui.genre.GenreActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var adapter: RecAdapter
    private val recommendations = mutableListOf<BookResponse>()

    private lateinit var database: DatabaseReference
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val cartIcon: ImageView = root.findViewById(R.id.imageView2)
        val imageAll: ImageView = root.findViewById(R.id.imageAll)

        // Setup ViewPager2 untuk Carousel
        viewPager = binding.viewpagerSlider
        setupImageCarousel()

        recyclerView = binding.viewRecom
        searchBar = binding.searchBar
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        adapter = RecAdapter(recommendations)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/")
            .getReference("2/data/")

        database.limitToFirst(10).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recommendations.clear()
                for (dataSnapshot in snapshot.children) {
                    val book = dataSnapshot.getValue(BookResponse::class.java)
                    book?.let { recommendations.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })

        imageAll.setOnClickListener{
            //pindah ke ActivityGenre
            val intent = Intent(requireContext(), GenreActivity::class.java)
            startActivity(intent)
        }

        cartIcon.setOnClickListener {
            // Pindah ke ActivityCart
            val intent = Intent(requireContext(), CartActivity::class.java)
            startActivity(intent)

        searchBar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    val intent = Intent(activity, SearchResultActivity::class.java)
                    intent.putExtra("SEARCH_QUERY", query)
                    startActivity(intent)
                }
                true
            } else {
                false
            }
        }

            searchBar.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        return root
    }

    private fun setupImageCarousel() {
        val images = listOf(
            R.drawable.carousel_2,
            R.drawable.carousel_3
        )

        viewPager.adapter = object : RecyclerView.Adapter<SimpleViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
                val imageView = ImageView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                return SimpleViewHolder(imageView)
            }

            override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
                (holder.itemView as ImageView).setImageResource(images[position])
            }

            override fun getItemCount(): Int = images.size
        }
    }

    private class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

