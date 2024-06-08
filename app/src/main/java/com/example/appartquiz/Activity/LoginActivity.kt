package com.example.appartquiz.Activity

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.example.appartquiz.databinding.ActivityLoginBinding
import com.example.appartquiz.Util.UiUtil

class LoginActivity : AppCompatActivity() {

    lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance().currentUser?.let {
            //user is there logged in
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        binding.loginBtn.setOnClickListener {
            login()
        }

        binding.backToIntro.setOnClickListener {
            startActivity(Intent(this,IntroActivity::class.java))
            finish()
        }

    }

    fun setInProgress(inProgress : Boolean){
        if(inProgress){
            binding.loginBtn.visibility = View.GONE
        }else{
            binding.loginBtn.visibility = View.VISIBLE
        }
    }
    fun login(){
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInput.setError("Email not valid")
            return;
        }
        if(password.length<6){
            binding.passwordInput.setError("Minimum 6 character")
            return
        }

        loginWithFirebase(email,password)
    }

    fun loginWithFirebase(email :String, password : String){
        setInProgress(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            email,
            password
        ).addOnSuccessListener {
            UiUtil.showToast(this,"Login successfully")
            setInProgress(false)
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }.addOnFailureListener {
            UiUtil.showToast(applicationContext,it.localizedMessage?: "Something went wrong")
            setInProgress(false)
        }
    }
}













