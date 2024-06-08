package com.example.appartquiz.Util

import android.content.Context
import android.os.Message
import android.widget.Toast
object UiUtil {
    fun showToast(context: Context, message: String){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }
}