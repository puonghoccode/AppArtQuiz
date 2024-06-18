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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private var userId: String? = null
    private var currentUserId: String? = null
    private lateinit var photoLauncher: ActivityResultLauncher<Intent>
    private var profileUserModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToMainBtn.setOnClickListener {
            finish()
        }


        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        if (currentUser != null) {
            val currentUserId = currentUser.uid
            Log.d("DEBUG", "Current User ID: $currentUserId")
            userId = currentUserId
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(currentUserId)
            getUserDataFromFireBase(currentUserId)

            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val userId = document.getString("userId")
                        if (userId != null) {
                            Log.d("DEBUG", "User ID from Firestore: $userId")
                            // Use userId here
                        } else {
                            Log.d("DEBUG", "User ID is null in Firestore document")
                        }
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

        photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                uploadToFirestore(result.data?.data!!)
            }
        }


        if (userId == currentUserId) {

            binding.changeProfilePic.setOnClickListener {
                checkPermissionAndPickPhoto()
            }
            binding.changeEmail.setOnClickListener {
                val changeEmailIntent = Intent(this, ChangeEmailActivity::class.java)
                startActivity(changeEmailIntent)
            }

            binding.profileBtn.text = "Logout"
            binding.profileBtn.setOnClickListener {
                logout()
            }
        } else {
            // Handle the case where userId and currentUserId are not equal
        }

    }

    fun logout(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this,LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun getUserDataFromFireBase(userId: String) {
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
        if (profileUserModel!= null) {
            profileUserModel?.apply {
                binding.profileUsername.text = "@" + username
                binding.profileEmail.text = email
                // Load profile picture using Glide
                Glide.with(binding.profilePic)
                    .load(profilePic)
                    .apply(RequestOptions.circleCropTransform()
                        .placeholder(R.drawable.ic_account) // Display ic_account if profilePic is null
                        .error(R.drawable.ic_account) // Display ic_account if there's an error loading the image
                    )
                    .into(binding.profilePic)

                binding.progressBar.visibility = View.GONE
            }
        } else {
            // Handle the case where profileUserModel is null
            binding.profileUsername.text = "@guest"
            Glide.with(binding.profilePic)
                .load(R.drawable.ic_account)
                .into(binding.profilePic)
            binding.progressBar.visibility = View.GONE
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
                    //video model store in firebase firestore
                    postToFirestore(downloadUrl.toString())
                }
            }
    }

    fun postToFirestore(url : String){
        currentUserId?.let {
            Firebase.firestore.collection("users")
                .document(it)
                .update("profilePic",url)
                .addOnSuccessListener {
                    profileUserModel!!.profilePic = url
                    setUI()
                }
        }
    }

    fun checkPermissionAndPickPhoto(){
        var readExternalPhoto : String = ""
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            readExternalPhoto = android.Manifest.permission.READ_MEDIA_IMAGES
        }else{
            readExternalPhoto = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if(ContextCompat.checkSelfPermission(this,readExternalPhoto)== PackageManager.PERMISSION_GRANTED){
            //we have permission
            openPhotoPicker()
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(readExternalPhoto),
                100
            )
        }
    }

    private fun openPhotoPicker(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }

}