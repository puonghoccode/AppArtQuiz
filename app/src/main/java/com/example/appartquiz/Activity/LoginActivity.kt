package com.example.appartquiz.Activity

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Patterns
import android.view.View
import com.example.appartquiz.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.example.appartquiz.databinding.ActivityLoginBinding
import com.example.appartquiz.Util.UiUtil
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity() : AppCompatActivity(), Parcelable {

    lateinit var binding : ActivityLoginBinding

    constructor(parcel: Parcel) : this() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance().currentUser?.let { user ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("username", user.email?.substringBefore("@"))
            startActivity(intent)
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoginActivity> {
        override fun createFromParcel(parcel: Parcel): LoginActivity {
            return LoginActivity(parcel)
        }

        override fun newArray(size: Int): Array<LoginActivity?> {
            return arrayOfNulls(size)
        }
    }

    fun loginWithFirebase(email :String, password : String) {
        setInProgress(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            email,
            password
        ).addOnSuccessListener { it.user?.let { user ->
            val userModel = UserModel(user.uid, email, email.substringBefore("@"))
            Firebase.firestore.collection("users").document(user.uid).set(userModel).addOnSuccessListener {
                UiUtil.showToast(this, "Login successfully")
                setInProgress(false)
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user_id", user.uid)
                intent.putExtra("user_email", email)
                intent.putExtra("username", email.substringBefore(""))
                startActivity(intent)
                finish()
            }.addOnFailureListener {UiUtil.showToast(applicationContext, it.localizedMessage ?: "Something went wrong")
                setInProgress(false)
                }
            }
        }
    }
}













