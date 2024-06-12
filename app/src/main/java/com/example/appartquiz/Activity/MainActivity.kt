package com.example.appartquiz.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.appartquiz.Adapter.QuizListAdapter
import com.example.appartquiz.Model.QuizModel
import com.example.appartquiz.Model.UserModel
import com.example.appartquiz.R
import com.example.appartquiz.Util.UiUtil
import com.google.firebase.database.FirebaseDatabase
import com.example.appartquiz.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    //quiz act
    lateinit var binding: ActivityMainBinding
    lateinit var quizModelList : MutableList<QuizModel>
    lateinit var adapter: QuizListAdapter

    //setting act

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingBtn.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                val intent = Intent(this, SettingActivity::class.java)
                intent.putExtra("us er_id", user.uid)
                intent.putExtra("username", user.email?.substringBefore("@"))
                intent.putExtra("email", user.email)
                startActivity(intent)
            }
        }

        setupUI()
        initRecyclerView()
        }

    private fun setupUI() {
        val userId = intent.getStringExtra("user_id")
        val username = intent.getStringExtra("username")

        binding.userNameTxt.text = "Welcome, $username!"

        binding.settingBtn.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun initRecyclerView() {
        quizModelList = mutableListOf()
        getDataFromFirebase()
    }

    //quiz act
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
    //end quiz act
}
