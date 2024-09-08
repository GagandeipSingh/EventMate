package com.example.eventMate.activities

import android.annotation.SuppressLint
import android.content.Intent
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
import com.example.eventMate.domains.UserDomain
import com.example.eventMate.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class RegisterAct : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        val rootView = window.decorView.rootView
        rootView.setOnApplyWindowInsetsListener { _, windowInsets ->
            val imeVisible = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom > 0
            if (imeVisible) {
                binding.constraintLayout2.setPadding(0,120,0,860)
            } else {
                binding.constraintLayout2.setPadding(0,120,0,0)
            }
            windowInsets
        }
        auth = Firebase.auth
        binding.loginTxt.setOnClickListener {
            startActivity(Intent(this@RegisterAct, LoginAct::class.java))
            finish()
        }
        binding.signUp.setOnClickListener {
            registerUser()
        }
        binding.etName.doOnTextChanged { _, _, _, _ ->
            binding.etNameLayout.isErrorEnabled = false
        }
        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            binding.etEmailLayout.isErrorEnabled = false
        }
        binding.etPassword.doOnTextChanged { _, _, _, _ ->
            binding.etPasswordLayout.isErrorEnabled = false
        }
        binding.etCPassword.doOnTextChanged { _, _, _, _ ->
            binding.etCPasswordLayout.isErrorEnabled = false
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val cPassword = binding.etCPassword.text.toString()
        val userDomainData = UserDomain(name, email)
        if (validateForm(name, email, password, cPassword)) {
            showProgressBar()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val database = Firebase.database
                        val myRef = database.getReference("Users").child(auth.currentUser!!.uid)
                        myRef.setValue(userDomainData).addOnSuccessListener {
                            Toast.makeText(this, "Successful.. Login Now..", Toast.LENGTH_SHORT).show()
                            dismissProgessBar()
                            startActivity(Intent(this, LoginAct::class.java))
                            finish()
                        }
                    } else {
                        val exception = task.exception
                        when (exception) {
                            is FirebaseAuthWeakPasswordException -> {
                                Toast.makeText(this, "The password is too weak.", Toast.LENGTH_SHORT).show()
                            }

                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
                            }

                            is FirebaseAuthUserCollisionException -> {
                                Toast.makeText(this, "Email is already in use.", Toast.LENGTH_SHORT).show()
                            }

                            else -> {
                                Toast.makeText(this, "Oops! Something went wrong", Toast.LENGTH_SHORT).show()
                            }
                        }
                        dismissProgessBar()
                    }
                }
        }
    }

    private fun validateForm(name: String, email: String, password: String, cPassword: String): Boolean {
        var correct = true
        if (TextUtils.isEmpty(name)) {
            binding.etNameLayout.error = "Enter Name.."
            correct = false
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailLayout.error = "Enter valid Email Address.."
            correct = false
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPasswordLayout.error = "Enter Password.."
            correct = false
        }
        if (TextUtils.isEmpty(cPassword)) {
            binding.etCPasswordLayout.error = "Enter Password.."
            correct = false
        }
        if (password != cPassword) {
            Toast.makeText(this@RegisterAct, "Passwords didn't match..", Toast.LENGTH_SHORT).show()
            correct = false
        }
        return correct
    }
}