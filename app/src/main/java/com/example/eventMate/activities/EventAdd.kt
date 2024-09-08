package com.example.eventMate.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.eventMate.progress.BaseActivity
import com.example.eventMate.domains.EventDomain
import com.example.eventMate.R
import com.example.eventMate.domains.UserDomain
import com.example.eventMate.databinding.ActivityEventAddBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventAdd : BaseActivity() {
    private lateinit var binding : ActivityEventAddBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var userName :String
    private lateinit var userID :String
    private var editEvent : String? = null
    private val calendar : Calendar = Calendar.getInstance()
    private var selectedDate : Date = calendar.time
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventAddBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        editEvent = intent.getStringExtra("edit")
        val gson = Gson()
        val objEvent = gson.fromJson(intent.getStringExtra("objString"), EventDomain::class.java)
        if(editEvent == "EditEvent"){
            binding.add.text = getString(R.string.edit)
            binding.saveButton.text = getString(R.string.update)
            binding.Title.setText(objEvent.name)
            binding.description.setText(objEvent.description)
            binding.venue.setText(objEvent.venue)
            binding.date.text = objEvent.date
            binding.time.text = objEvent.time
            binding.deadline.text = objEvent.deadline
            selectedDate = objEvent.selectedDate
        }
        auth = Firebase.auth
        val userId = auth.currentUser?.uid
        userID = userId.toString()
        if (userId != null) {
            val database = Firebase.database
            val myRef = database.getReference("Users").child(userId)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userDomainData = dataSnapshot.getValue(UserDomain::class.java)
                    val user = userDomainData?.name
                    userName = user ?: "Creator"
                }
                override fun onCancelled(databaseError: DatabaseError) {} })
        }
        val rootView = window.decorView.rootView
        rootView.setOnApplyWindowInsetsListener { _, windowInsets ->
            val imeVisible = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom > 0
            if (imeVisible) {
                binding.constraintLayout2.setPadding(30,0,30,880)
            } else {
                binding.constraintLayout2.setPadding(30,0,30,60)
            }
            windowInsets
        }
        binding.Title.doOnTextChanged { _, _, _, _ ->
            binding.tilTitle.isErrorEnabled = false
        }
        binding.description.doOnTextChanged { _, _, _, _ ->
            binding.tilDescription.isErrorEnabled = false
        }
        binding.venue.doOnTextChanged { _, _, _, _ ->
            binding.tilVenue.isErrorEnabled = false
        }
        binding.date.setOnClickListener {

            // Create the DatePickerDialog with the specified date range
            DatePickerDialog(this, { _, year, month, date ->
                calendar.set(year, month, date)
                selectedDate = calendar.time
                val simpleDateFormat = SimpleDateFormat("dd / MM / yy", Locale.getDefault())
                val formattedDate = simpleDateFormat.format(selectedDate)
                binding.date.text = formattedDate
            },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
                .apply {
                    val calTemp = Calendar.getInstance()
                    datePicker.minDate = calTemp.timeInMillis
                }.show()
            binding.venue.clearFocus()
        }
        binding.time.setOnClickListener {
            if(binding.date.text == getString(R.string.date)){
                Toast.makeText(this@EventAdd,"Select Event Date First..",Toast.LENGTH_SHORT).show()
            }
            else{
                TimePickerDialog(this, { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    val currentTime = Calendar.getInstance()
                    if (calendar.before(currentTime)) {
                        Toast.makeText(this@EventAdd,"Time cannot be in the past",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val simpleTimeFormat = SimpleDateFormat("hh : mm  a", Locale.getDefault()) // Or your desired locale
                        val formattedTime = simpleTimeFormat.format(calendar.time)
                        binding.time.text = formattedTime
                    }
                },
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    false)
                    .show()
            }
        }
        binding.deadline.setOnClickListener {
            if(binding.date.text == getString(R.string.date)){
                Toast.makeText(this@EventAdd,"Select Event Date First..",Toast.LENGTH_SHORT).show()
            }
            else{
                val cal = Calendar.getInstance()
                DatePickerDialog(this, { _, year, month, date ->
                    cal.set(year, month, date)
                    val selectedDate = cal.time
                    val simpleDateFormat = SimpleDateFormat("dd / MM / yy", Locale.getDefault())
                    val formattedDate = simpleDateFormat.format(selectedDate)
                    binding.deadline.text = formattedDate
                },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                    .apply {
                        val calTemp = Calendar.getInstance()
                        datePicker.minDate = calTemp.timeInMillis
                        datePicker.maxDate = selectedDate.time
                    }.show()
                binding.venue.clearFocus()
            }
        }
        binding.saveButton.setOnClickListener {
            if(editEvent == "EditEvent"){
                updateEvent(objEvent)
            }
            else{
                if(validateDetails()){
                    showProgressBar()
                    val databaseRef = Firebase.database.getReference("Events")
                    databaseRef.push().setValue(
                        EventDomain(binding.Title.text.toString(),binding.description.text.toString(),userName,
                        userID,binding.date.text.toString(),binding.time.text.toString(),binding.deadline.text.toString(),selectedDate,binding.venue.text.toString(),Calendar.getInstance().timeInMillis,"")
                    )
                        .addOnCompleteListener {
                            Toast.makeText(this@EventAdd,"New Event is added..",Toast.LENGTH_SHORT).show()
                            finish()
                            dismissProgessBar()
                        }
                }
            }
        }
    }

    private fun updateEvent(objEvent : EventDomain) {
        val updated = HashMap<String,Any>()
        if(validateDetails()){
            updated["name"] = binding.Title.text.toString()
            updated["description"] = binding.description.text.toString()
            updated["venue"] = binding.venue.text.toString()
            updated["date"] = binding.date.text.toString()
            updated["time"] = binding.time.text.toString()
            updated["deadline"] = binding.deadline.text.toString()
            updated["selectedDate"] = selectedDate
            showProgressBar()
            val eventRef = Firebase.database.getReference("Events").child(objEvent.key)
            eventRef.updateChildren(updated)
                .addOnCompleteListener {
                    dismissProgessBar()
                    Toast.makeText(this@EventAdd,"Event is Updated..",Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun validateDetails(): Boolean {
        var isCorrect = true
        if(binding.Title.text.trim().isEmpty()){
            isCorrect = false
            binding.Title.text = null
            binding.tilTitle.error = "Enter the Title.."
        }
        else if(binding.description.text.trim().isEmpty()){
            isCorrect = false
            binding.description.text = null
            binding.tilDescription.error = "Enter the Description.."
        }
        else if(binding.venue.text.trim().isEmpty()){
            isCorrect = false
            binding.venue.text = null
            binding.tilVenue.error = "Enter the Title.."
        }
        else if(binding.date.text == getString(R.string.date)){
            isCorrect = false
            Toast.makeText(this@EventAdd,"Select Event Date..",Toast.LENGTH_SHORT).show()
        }
        else if(binding.time.text == getString(R.string.time)){
            isCorrect = false
            Toast.makeText(this@EventAdd,"Select Time..",Toast.LENGTH_SHORT).show()
        }
        else if(binding.deadline.text == getString(R.string.deadline)){
            isCorrect = false
            Toast.makeText(this@EventAdd,"Select Deadline..",Toast.LENGTH_SHORT).show()
        }
        else if(binding.date.text.toString() < binding.deadline.text.toString()){
            isCorrect = false
            Toast.makeText(this@EventAdd,"Event date must follow deadline.",Toast.LENGTH_SHORT).show()
        }
        return isCorrect
    }

}