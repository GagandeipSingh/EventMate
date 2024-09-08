package com.example.eventMate.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.eventMate.R
import com.example.eventMate.databinding.ActivityDetailsEnrollBinding
import com.example.eventMate.domains.EventDomain
import com.example.eventMate.domains.RegDetailsDomain
import com.example.eventMate.domains.UserDomain
import com.example.eventMate.progress.BaseActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class DetailsEnroll : BaseActivity() {
    private lateinit var binding: ActivityDetailsEnrollBinding
    private var eventID: String? = null
    private lateinit var userName: String
    private lateinit var userEmail: String
    private var isIDPresent : Boolean = false
    private lateinit var currentDate : String

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsEnrollBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        eventID = intent.getStringExtra("EventID")
        currentDate = intent.getStringExtra("currentDate").toString()
        val rootView = window.decorView.rootView
        rootView.setOnApplyWindowInsetsListener { _, windowInsets ->
            val imeVisible = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom > 0
            if (imeVisible) {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, 680)
                }
            } else {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, 0)
                }
            }
            windowInsets
        }
        binding.cancel.setOnClickListener {
            finish()
        }
        val database = Firebase.database
        val myRef = database.getReference("Users").child(Firebase.auth.currentUser!!.uid)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userDomainData = dataSnapshot.getValue(UserDomain::class.java)
                userName = userDomainData?.name.toString()
                userEmail = userDomainData?.email.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        binding.saveButton.setOnClickListener {
            registerDetails()
        }
        binding.Id.doOnTextChanged { _, _, _, _ ->
            binding.tilId.isErrorEnabled = false
        }
        binding.Class.doOnTextChanged { _, _, _, _ ->
            binding.tilClass.isErrorEnabled = false
        }
        binding.department.doOnTextChanged { _, _, _, _ ->
            binding.tilDepartment.isErrorEnabled = false
        }
    }

    private fun registerDetails() {
        if (validate(binding)) {
            showProgressBar()
            val ref = Firebase.database.getReference("Events").child(eventID.toString())
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val eventId = snapshot.getValue(EventDomain::class.java)
                    val creatorId = eventId!!.creatorId
                    if(creatorId == Firebase.auth.currentUser!!.uid){
                        Toast.makeText(this@DetailsEnroll, "You're the organizer of this event..", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else{
                        val eventRef = Firebase.database.getReference("Events").child(eventID.toString()).child("Registrations")
                        eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (registrationId in snapshot.children) {
                                    if ((registrationId.child("userId").value ==  Firebase.auth.currentUser!!.uid) ||
                                        registrationId.child("studentId").value == binding.Id.text.trim().toString()) {
                                        Toast.makeText(this@DetailsEnroll, "You have already registered..", Toast.LENGTH_SHORT).show()
                                        finish()
                                        isIDPresent = true
                                        dismissProgessBar()
                                        break
                                    }
                                }
                                if(!isIDPresent){
                                    eventRef.push().setValue((RegDetailsDomain(userName, userEmail, Firebase.auth.currentUser!!.uid,binding.Id.text.trim().toString(),
                                        binding.Class.text.trim().toString(), binding.department.text.trim().toString(),currentDate)))
                                        .addOnCompleteListener {
                                            Toast.makeText(this@DetailsEnroll, "Your Registration is Done..", Toast.LENGTH_SHORT).show()
                                            val sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)
                                            val editor = sharedPreferences.edit()
                                            editor.putBoolean("send",true)
                                            editor.apply()
                                            finish()
                                            dismissProgessBar()
                                        }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {} })
                    }
                }
                override fun onCancelled(error: DatabaseError) {} })
        }
    }

    private fun validate(binding: ActivityDetailsEnrollBinding): Boolean {
        var isCorrect = true
        if (binding.Id.text.trim().isEmpty()) {
            isCorrect = false
            binding.Id.text = null
            binding.tilId.error = "Enter Your ID.."
        } else if (binding.Class.text.trim().isEmpty()) {
            isCorrect = false
            binding.Class.text = null
            binding.tilClass.error = "Enter Your Class.."
        } else if (binding.department.text.trim().isEmpty()) {
            isCorrect = false
            binding.department.text = null
            binding.tilDepartment.error = "Enter Your Class.."
        }
        return isCorrect
    }


}