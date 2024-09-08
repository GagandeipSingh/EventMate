package com.example.eventMate.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.eventMate.progress.BaseActivity
import com.example.eventMate.R
import com.example.eventMate.databinding.ActivityPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class PasswordAct : BaseActivity() {
    private lateinit var binding: ActivityPasswordBinding
    private lateinit var  auth : FirebaseAuth
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = Firebase.auth
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left,0, systemBars.right, systemBars.bottom)
            insets
        }
        val rootView = window.decorView.rootView
        rootView.setOnApplyWindowInsetsListener { _, windowInsets ->
            val imeVisible = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom > 0
            if (imeVisible) {
                binding.constraintLayout2.setPadding(0,140,0,860)
            } else {
                binding.constraintLayout2.setPadding(0,140,0,0)
            }
            windowInsets
        }
        binding.submit.setOnClickListener {
            resetPassword()
        }
        binding.etForgotPasswordEmail.doOnTextChanged { _, _, _, _ ->
            binding.tilEmailForgetPassword.isErrorEnabled = false
        }
    }

    private fun resetPassword(){
        val email = binding.etForgotPasswordEmail.text.toString()
        if(validateForm(email)){
            showProgressBar()
            auth.sendPasswordResetEmail(email).addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    dismissProgessBar()
                    binding.etForgotPasswordEmail.visibility = View.INVISIBLE
                    binding.tvSubmitMsg.visibility = View.VISIBLE
                    binding.tilEmailForgetPassword.visibility = View.INVISIBLE
                    binding.submit.visibility = View.INVISIBLE
                }
                else{
                    dismissProgessBar()
                    showToast(this,"Cannot Reset your Password..")
                }
            }
        }
    }
    private fun validateForm(email:String):Boolean
    {
        return when {
            TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()->{
                binding.tilEmailForgetPassword.error = "Enter valid email address"
                false
            }
            else -> { true }
        }
    }
}