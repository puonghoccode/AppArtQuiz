package com.example.appartquiz.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.appartquiz.Adapter.QuizListAdapter
import com.example.appartquiz.Model.QuizModel
import com.example.appartquiz.Model.UserModel
import com.example.appartquiz.R
import com.google.firebase.database.FirebaseDatabase
import com.example.appartquiz.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var quizModelList : MutableList<QuizModel>
    lateinit var adapter: QuizListAdapter
    private lateinit var welcomeTextView: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        welcomeTextView = binding.welcomeTextView
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore


        binding.settingBtn.setOnClickListener{
            val intent = Intent(this,SettingActivity::class.java)
            startActivity(intent)
        }

        quizModelList = mutableListOf()
        getDataFromFirebase()
        getUsernameFromFirestore()
    }

    private fun getUsernameFromFirestore() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userModel = document.toObject(UserModel::class.java)
                        binding.welcomeTextView.text = "Welcome, ${userModel?.username}!"
                    }
                }
        }
    }


    private fun setupRecyclerView(){
        binding.progressBar.visibility = View.GONE
        adapter = QuizListAdapter(quizModelList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromFirebase(){
        binding.progressBar.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().reference
            .get()
            .addOnSuccessListener { dataSnapshot->
                if(dataSnapshot.exists()){
                    for (snapshot in dataSnapshot.children){
                        val quizModel = snapshot.getValue(QuizModel::class.java)
                        if (quizModel != null) {
                            quizModelList.add(quizModel)
                        }
                    }
                }
                setupRecyclerView()
            }
    }
}