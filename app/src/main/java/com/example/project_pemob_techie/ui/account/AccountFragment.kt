package com.example.project_pemob_techie.ui.account

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.FragmentAccountBinding
import com.example.project_pemob_techie.ui.login.Login
import com.example.project_pemob_techie.ui.login.VerifyEmail_ChangePW
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root
        database = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("techbook_techie/")

        binding.imageView31.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.button10.setOnClickListener {
            val intent = Intent(requireContext(), VerifyEmail_ChangePW::class.java)
            startActivity(intent)
        }

        binding.button9.setOnClickListener {
            logoutUser()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database.child("user").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").value.toString()
                        val username = snapshot.child("username").value.toString()
                        val email = snapshot.child("email").value.toString()
                        val phone = snapshot.child("phone").value.toString()
                        val birthday = snapshot.child("birthday").value.toString()
                        val language = "English"
                        val profileImageBase64 = snapshot.child("profileImageUrl").value.toString()

                        binding.textView61.text = name
                        binding.textView65.text = username
                        binding.textView78.text = email
                        binding.textView79.text = phone
                        binding.textView80.text = birthday
                        binding.textView81.text = language

                        if (profileImageBase64.isNotEmpty()) {
                            try {
                                val decodedString = android.util.Base64.decode(profileImageBase64, android.util.Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                                binding.imageView30.setImageBitmap(bitmap)
                            } catch (e: Exception) {
                                binding.imageView30.setImageResource(R.drawable.profile)
                                e.printStackTrace()
                            }
                        } else {
                            binding.imageView30.setImageResource(R.drawable.profile)
                        }
                    } else {
                        Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        SessionManager.endSession(requireContext())
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(requireContext(), "You have been logged out.", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
