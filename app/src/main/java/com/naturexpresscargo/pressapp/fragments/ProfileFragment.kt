package fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.pressapp.ContactUsActivity
import com.naturexpresscargo.pressapp.PrivacyPolicyActivity
import com.naturexpresscargo.pressapp.WelcomeActivity
import com.naturexpresscargo.pressapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var profileListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadUserProfile()
        setupClickListeners()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            _binding?.let { it.progressBar.visibility = View.VISIBLE }

            val userId = currentUser.uid
            val userRef = database.reference.child("users").child(userId)

            profileListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _binding?.let { binding ->
                        binding.progressBar.visibility = View.GONE

                        if (snapshot.exists()) {
                            val fullName = snapshot.child("fullName").getValue(String::class.java) ?: "User"
                            val email = snapshot.child("email").getValue(String::class.java) ?: currentUser.email ?: ""

                            binding.tvUserName.text = fullName
                            binding.tvUserEmail.text = email

                            val initials = getInitials(fullName)
                            binding.tvInitials.text = initials
                        } else {
                            binding.tvUserName.text = currentUser.email?.substringBefore("@") ?: "User"
                            binding.tvUserEmail.text = currentUser.email ?: ""
                            binding.tvInitials.text = getInitials(currentUser.email ?: "U")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _binding?.let { binding ->
                        binding.progressBar.visibility = View.GONE
                    }
                    context?.let {
                        Toast.makeText(
                            it,
                            "Failed to load profile: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            userRef.addValueEventListener(profileListener!!)
        }
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "U"
        }
    }

    private fun setupClickListeners() {
        binding.cardTermsConditions.setOnClickListener {
            val intent = Intent(requireContext(), PrivacyPolicyActivity::class.java)
            intent.putExtra(
                PrivacyPolicyActivity.EXTRA_URL,
                PrivacyPolicyActivity.TERMS_CONDITIONS_URL
            )
            intent.putExtra(
                PrivacyPolicyActivity.EXTRA_TITLE,
                "Terms and Conditions"
            )
            startActivity(intent)
        }

        binding.cardPrivacyPolicy.setOnClickListener {
            val intent = Intent(requireContext(), PrivacyPolicyActivity::class.java)
            intent.putExtra(
                PrivacyPolicyActivity.EXTRA_URL,
                PrivacyPolicyActivity.PRIVACY_POLICY_URL
            )
            intent.putExtra(
                PrivacyPolicyActivity.EXTRA_TITLE,
                "Privacy Policy"
            )
            startActivity(intent)
        }

        binding.cardContactUs.setOnClickListener {
            val intent = Intent(requireContext(), ContactUsActivity::class.java)
            startActivity(intent)
        }

        binding.cardDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showDeleteAccountDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently deleted.")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteAccount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun deleteAccount() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        _binding?.let { it.progressBar.visibility = View.VISIBLE }

        val userId = currentUser.uid

        // Delete user data from database
        database.reference.child("users").child(userId).removeValue()
            .addOnSuccessListener {
                // Delete user authentication account
                currentUser.delete()
                    .addOnSuccessListener {
                        _binding?.let { it.progressBar.visibility = View.GONE }
                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()

                        // Navigate to welcome screen
                        val intent = Intent(requireContext(), WelcomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        activity?.finish()
                    }
                    .addOnFailureListener { exception ->
                        _binding?.let { it.progressBar.visibility = View.GONE }
                        Toast.makeText(
                            requireContext(),
                            "Failed to delete account: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { exception ->
                _binding?.let { it.progressBar.visibility = View.GONE }
                Toast.makeText(
                    requireContext(),
                    "Failed to delete user data: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firebase listener to prevent memory leaks
        profileListener?.let {
            auth.currentUser?.uid?.let { userId ->
                database.reference.child("users").child(userId).removeEventListener(it)
            }
        }
        _binding = null
    }
}

