package com.example.appartquiz.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.appartquiz.Model.UserModel
import com.example.appartquiz.R
import com.example.appartquiz.databinding.ActivitySettingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SettingActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivitySettingBinding
    private var userId: String? = null
    private var currentUserId: String? = null
    private var profileUserModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.changeProfilePic.setOnClickListener {
            val changeProfilePicIntent = Intent(this, ChangeUserProfilePicActivity::class.java)
            startActivity(changeProfilePicIntent)
        }

        binding.viewResult.setOnClickListener {
            val viewResultIntent = Intent(this, ViewResultActivity::class.java)
            startActivity(viewResultIntent)
        }

        binding.deleteBtn.setOnClickListener {
            confirmDeleteAccount()
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)

        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        if (currentUser != null) {
            currentUserId = currentUser.uid
            Log.d("DEBUG", "Current User ID: $currentUserId")
            userId = currentUserId
            getUserDataFromFirebase(currentUserId!!)

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            val userId = document.getString("userId")
                            Log.d("DEBUG", "User ID from Firestore: $userId")
                        } else {
                            Log.d("DEBUG", "User document does not exist in Firestore")
                        }
                    } else {
                        Log.d("DEBUG", "Error getting user document: ${task.exception}")
                        if (task.exception is FirebaseFirestoreException) {
                            val firestoreException = task.exception as FirebaseFirestoreException
                            Log.d("DEBUG", "Firestore error code: ${firestoreException.code}")
                        }
                    }
                }
        } else {
            Log.d("DEBUG", "Current User is null")
        }

        if (userId == currentUserId) {
            binding.changeUsername.setOnClickListener {
                val changeUserNameIntent = Intent(this, ChangeUserNameActivity::class.java)
                startActivity(changeUserNameIntent)
            }
            binding.changePassword.setOnClickListener {
                val changePasswordIntent = Intent(this, ChangePasswordActivity::class.java)
                startActivity(changePasswordIntent)
            }
            binding.profileBtn.text = "Logout"
            binding.profileBtn.setOnClickListener {
                logout()
            }
        }
    }

    private fun logout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sign out ??")
            .setMessage("Are you sure you want to sign out this account ?")
            .setPositiveButton("Yes") { dialog, which ->
                Firebase.auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getUserDataFromFirebase(userId: String) {
        Firebase.firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    profileUserModel = document.toObject(UserModel::class.java)
                    setUI()
                }
            }
    }

    private fun setUI() {
        profileUserModel?.let {
            binding.profileUsername.text = "@${it.username}"
            binding.profileEmail.text = it.email
            Glide.with(binding.profilePic)
                .load(it.profilePic)
                .apply(
                    RequestOptions.circleCropTransform()
                        .placeholder(R.drawable.ic_account)
                        .error(R.drawable.ic_account)
                )
                .into(binding.profilePic)
            binding.progressBar.visibility = View.GONE
        } ?: run {
            binding.profileUsername.text = "@guest"
            Glide.with(binding.profilePic)
                .load(R.drawable.ic_account)
                .into(binding.profilePic)
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_discover -> {
                val intent = Intent(this, DiscoveryActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.navigation_favorites -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.navigation_settings -> {
                // Already in settings
                return true
            }
        }
        return false
    }

    private fun confirmDeleteAccount() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Account ??")
            .setMessage("Are you sure you want to delete this account? This action cannot be undone.")
            .setPositiveButton("Yes") { dialog, which ->
                deleteAccount()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            Firebase.firestore.collection("users").document(it.uid)
                .delete()
                .addOnSuccessListener {
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("DEBUG", "User account deleted.")
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("ERROR", "Failed to delete user account: ${task.exception?.message}")
                                Toast.makeText(this, "Failed to delete user account.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("ERROR", "Failed to delete user data from Firestore: ${e.message}")
                    Toast.makeText(this, "Failed to delete user data.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
