package com.example.eventMate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.eventMate.progress.BaseActivity
import com.example.eventMate.R
import com.example.eventMate.databinding.ActivityAccountIdBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class AccountId : BaseActivity() {
    private lateinit var binding: ActivityAccountIdBinding
    private var accountType: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountIdBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        binding.cancel.setOnClickListener {
            finish()
        }
        accountType = intent.getStringExtra("accountType")
        binding.userName.text = intent.getStringExtra("userName")
        binding.userEmail.text = intent.getStringExtra("userEmail")
        binding.etCodeText.doOnTextChanged { _, _, _, _ ->
            binding.tilCode.isErrorEnabled = false
        }
        showProgressBar()
        if (accountType == "Organiser") {
            val myRef = Firebase.database.getReference("OrganiserId")
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val organiserId = snapshot.getValue(String::class.java)
                    check(binding, organiserId)
                    dismissProgessBar()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } else {
            val myRef = Firebase.database.getReference("ParticipantId")
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val participantId = snapshot.getValue(String::class.java)
                    check(binding, participantId)
                    dismissProgessBar()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

    }

    private fun check(binding: ActivityAccountIdBinding, accountId: String?) {
        binding.submitBtn.setOnClickListener {
            if (binding.etCodeText.text.trim().isEmpty()) {
                binding.tilCode.error = "Enter ID.."
            } else {
                if (accountId != binding.etCodeText.text.trim().toString()) {
                    binding.tilCode.error = "InCorrect Id.."
                } else {
                    showProgressBar()
                    val myRef = Firebase.database.getReference("Users")
                        .child(Firebase.auth.currentUser!!.uid)
                    val updates = HashMap<String, Any>()
                    if (accountType == "Organiser") updates["account"] = "Organiser"
                    else updates["account"] = "Participant"
                    updates["selected"] = "1"
                    myRef.updateChildren(updates).addOnCompleteListener {
                        dismissProgessBar()
                        if (accountType == "Organiser")
                            Toast.makeText(
                                this@AccountId,
                                "Logged in as Organiser..",
                                Toast.LENGTH_SHORT
                            ).show()
                        else
                            Toast.makeText(
                                this@AccountId,
                                "Logged in as Participant..",
                                Toast.LENGTH_SHORT
                            ).show()
                        val intent = Intent(this, DashboardAct::class.java)
                        if (accountType == "Organiser") intent.putExtra("accountType", "Organiser")
                        else intent.putExtra("accountType", "Participant")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}