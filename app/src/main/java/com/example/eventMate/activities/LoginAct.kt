package com.example.eventMate.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.eventMate.progress.BaseActivity
import com.example.eventMate.R
import com.example.eventMate.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginAct : BaseActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val rootView = window.decorView.rootView
        rootView.setOnApplyWindowInsetsListener { _, windowInsets ->
            val imeVisible = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom > 0
            if (imeVisible) {
                binding.constraintLayout2.setPadding(0,160,0,860)
            } else {
                binding.constraintLayout2.setPadding(0,160,0,0)
            }
            windowInsets
        }
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)

        }
        binding.loginTxt.setOnClickListener {
            startActivity(Intent(this@LoginAct, RegisterAct::class.java))
        }
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this@LoginAct, PasswordAct::class.java))
        }
        binding.logIn.setOnClickListener {
            signInUser()
        }
        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            binding.etEmailLayout.isErrorEnabled = false
        }
        binding.etPassword.doOnTextChanged { _, _, _, _ ->
            binding.etPasswordLayout.isErrorEnabled = false
        }

    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()->{
                binding.etEmailLayout.error = "Enter valid email address"
                false
            }
            TextUtils.isEmpty(password)->{
                binding.etPasswordLayout.error = "Enter password"
                false
            }
            else -> { true }
        }
    }

    private fun signInUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        if(validateForm(email,password)){
            showProgressBar()
            firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        startActivity(Intent(this, UserType::class.java))
                        finish()
                        dismissProgessBar()
                    }
                    else{
                        Toast.makeText(this,"Check Email or Password carefully..", Toast.LENGTH_SHORT).show()
                        dismissProgessBar()
                    }
                }
        }
    }
}