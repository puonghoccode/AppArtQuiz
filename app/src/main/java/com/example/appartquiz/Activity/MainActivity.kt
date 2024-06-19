package com.example.appartquiz.Activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appartquiz.Adapter.QuizListAdapter
import com.example.appartquiz.Model.QuizModel
import com.example.appartquiz.Model.UserModel
import com.example.appartquiz.R
import com.example.appartquiz.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding
    lateinit var quizModelList: MutableList<QuizModel>
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

        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)

        quizModelList = mutableListOf()
        getDataFromFirebase()
        getUsernameFromFirestore()
    }

    private fun getUsernameFromFirestore() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userModel = document.toObject(UserModel::class.java)
                        binding.welcomeTextView.text = "Welcome, ${userModel?.username}!"
                    }
                }
        }
    }

    private fun setupRecyclerView() {
        binding.progressBar.visibility = View.GONE
        adapter = QuizListAdapter(quizModelList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().reference
            .child("quizzes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val quizModel = snapshot.getValue(QuizModel::class.java)
                            if (quizModel != null) {
                                quizModelList.add(quizModel)
                            }
                        }
                    }
                    setupRecyclerView()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors.
                }
            })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_discover -> {
                val intent = Intent(this, DiscoveryActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.navigation_favorites -> {
                // Handle favorites navigation
                return true
            }
            R.id.navigation_settings -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return false
    }
}
