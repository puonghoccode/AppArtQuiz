package com.example.appartquiz.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.example.appartquiz.Model.UserModel
import com.example.appartquiz.databinding.ActivitySignupBinding
import com.example.appartquiz.Util.UiUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupBtn.setOnClickListener {
            signup()
        }

        binding.backToIntro.setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }

    }

    fun setInProgress(inProgress: Boolean){
        if (inProgress){
            binding.signupBtn.visibility = View.GONE
        } else{
            binding.signupBtn.visibility = View.VISIBLE
        }
    }

    fun signup() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInput.error = "Email not valid"
            return
        }

        if (password.length<6){
            binding.passwordInput.error = "Minimum 6 characters"
            return
        }

        if (password!=confirmPassword){
            binding.confirmPasswordInput.error = "Not matched password"
            return
        }

        signupWithFirebase(email, password)
    }

    fun signupWithFirebase(email : String, password : String){
        setInProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener { it.user?.let {user->
                val userModel = UserModel( user.uid,email,email.substringBefore("@") )
                Firebase.firestore.collection("users")
                    .document(user.uid)
                    .set(userModel).addOnSuccessListener {
                        UiUtil.showToast(applicationContext,"Account created successfully")
                        setInProgress(false)
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                    }
            }
        }.addOnFailureListener {
            UiUtil.showToast(applicationContext,it.localizedMessage?: "Something went wrong")
            setInProgress(false)
        }
    }
}






