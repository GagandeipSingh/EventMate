package com.example.eventMate.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eventMate.progress.BaseActivity
import com.example.eventMate.R
import com.example.eventMate.domains.UserDomain
import com.example.eventMate.databinding.ActivityUserTypeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class UserType : BaseActivity() {
    private lateinit var binding: ActivityUserTypeBinding
    private lateinit var whichSelected : String
    private lateinit var userName : String
    private lateinit var userEmail : String
    private lateinit var auth : FirebaseAuth
    private lateinit var myRef : DatabaseReference
    private  var userId : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserTypeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        showProgressBar()
        auth  = Firebase.auth
        userId = auth.currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            myRef = database.getReference("Users").child(userId.toString())
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userDomainData = dataSnapshot.getValue(UserDomain::class.java)
                    userName = userDomainData!!.name
                    userEmail = userDomainData.email
                    dismissProgessBar()
                }
                override fun onCancelled(databaseError: DatabaseError) {} })
        }
        binding.organiser.setOnClickListener {
            whichSelected = "Organiser"
            binding.organiser.setBackgroundResource(R.drawable.green_border)
            binding.next.visibility = View.VISIBLE
            binding.organiserTxt.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
            binding.participant.setBackgroundResource(R.drawable.black_border)
            binding.participantTxt.setTextColor(ContextCompat.getColor(this, R.color.black_7))
        }
        binding.participant.setOnClickListener {
            whichSelected = "Participant"
            binding.participant.setBackgroundResource(R.drawable.green_border)
            binding.next.visibility = View.VISIBLE
            binding.participantTxt.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
            binding.organiser.setBackgroundResource(R.drawable.black_border)
            binding.organiserTxt.setTextColor(ContextCompat.getColor(this, R.color.black_7))
        }
        binding.cancel.setOnClickListener {
            finish()
        }
        binding.next.setOnClickListener {
            if(whichSelected == "Organiser"){
                check("Organiser")
            }
            if(whichSelected == "Participant"){
                check("Participant")
            }
        }
    }
    private fun check(account:String) {
        showProgressBar()

            val intent = Intent(this@UserType, AccountId::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("userEmail", userEmail)
            if (account == "Organiser") intent.putExtra("accountType","Organiser")
            else intent.putExtra("accountType","Participant")
            startActivity(intent)
            dismissProgessBar()
        }
        }