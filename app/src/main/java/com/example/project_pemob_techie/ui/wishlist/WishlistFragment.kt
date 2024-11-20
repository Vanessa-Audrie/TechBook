import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.FragmentWishlistBinding
import com.example.project_pemob_techie.ui.account.SessionManager
import com.example.project_pemob_techie.ui.wishlist.WishlistAdapter
import com.example.project_pemob_techie.ui.wishlist.WishlistItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

class WishlistFragment : Fragment() {
    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)

        val isLoggedIn = SessionManager.isLoggedIn(requireContext())
        if (!isLoggedIn) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        // Get userId from SessionManager
        userId = SessionManager.getUserId(requireContext()) ?: return binding.root

        // Fetch wishlist from Firebase
        val wishlistRef = FirebaseDatabase.getInstance().getReference("Wishlist").child(userId)
        wishlistRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wishlistItems = mutableListOf<WishlistItem>()
                for (itemSnapshot in snapshot.children) {
                    val bookId = itemSnapshot.key
                    val image = itemSnapshot.child("image").value as? String
                    val price = itemSnapshot.child("price").value as? String
                    if (bookId != null && price != null && image != null) {
                        // Pass Base64 string directly
                        wishlistItems.add(WishlistItem(bookId, image, price))
                    }
                }
                setupRecyclerView(wishlistItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load wishlist: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }

    private fun setupRecyclerView(wishlistItems: List<WishlistItem>) {
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = WishlistAdapter(wishlistItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Ensure binding is nullified after the view is destroyed
    }
}


