package com.example.appartquiz.Activity

import com.example.appartquiz.Model.UserModel
import com.example.appartquiz.R
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.appartquiz.databinding.ActivitySettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class SettingActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingBinding
    lateinit var userId:String
    lateinit var currentUserId: String
    lateinit var photoLauncher: ActivityResultLauncher<Intent>

    lateinit var profileUserModel : UserModel
    lateinit var userName: String
    lateinit var userEmail: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToMainBtn.setOnClickListener {
            Firebase.firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener {
                    FirebaseAuth.getInstance().currentUser?.let { user ->
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("user_id", user.uid)
                        intent.putExtra("username", user.email?.substringBefore("@"))
                        intent.putExtra("email", user.email)
                        startActivity(intent)
                    }
                }
        }

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val userId = intent.getStringExtra("user_id")
        val userName = intent.getStringExtra("username")

        if (userId != null && userName != null) {
            this.currentUserId = userId
            this.userName = userName

            binding.profileBtn.text = "Logout"
            binding.profileBtn.setOnClickListener {
                logout()
            }
            photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    uploadToFirestore(result.data?.data!!)
                }
            }

            getUserDataFromFireBase()
        } else {
            Log.e("Error", "Intent extras are null")
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun getUserDataFromFireBase() {
        Firebase.firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener {
                profileUserModel = it.toObject(UserModel::class.java)!!
                binding.usernameTxt.text = "@" + userName
                binding.userEmailTxt.text = profileUserModel.email
                binding.progressBar.visibility = View.VISIBLE

                Glide.with(binding.profilePic).load(profileUserModel.profilePic)
                    .apply(RequestOptions().placeholder(R.drawable.ic_account))
                    .into(binding.profilePic)
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Error getting user data: $exception")
            }
    }
    fun setUI() {
        if (profileUserModel != null) {
            Glide.with(binding.profilePic).load(profileUserModel.profilePic)
                .apply(RequestOptions().placeholder(R.drawable.ic_account))
                .into(binding.profilePic)
        } else {
            // Handle the case where profileUserModel is null
            Log.e("Error", "profileUserModel is null")
        }
    }

    fun uploadToFirestore(photoUri : Uri){
        binding.progressBar.visibility = View.VISIBLE
        val photoRef =  FirebaseStorage.getInstance()
            .reference
            .child("profilePic/"+ currentUserId )
        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener {downloadUrl->
                    postToFirestore(downloadUrl.toString())
                }
            }
    }

    fun postToFirestore(url: String) {
        Firebase.firestore.collection("users")
            .document(userId)
            .update("profilePic", url)
            .addOnSuccessListener {
                getUserDataFromFireBase()
            }
    }

    fun checkPermissionAndPickPhoto() {
        var readExternalPhoto: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readExternalPhoto = android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            readExternalPhoto = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, readExternalPhoto) == PackageManager.PERMISSION_GRANTED) {
            //we have permission
            openPhotoPicker()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(readExternalPhoto),
                100
            )
        }
    }

    private fun openPhotoPicker() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }

}