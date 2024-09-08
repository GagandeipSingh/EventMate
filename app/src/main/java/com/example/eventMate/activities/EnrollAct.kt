package com.example.eventMate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.eventMate.R
import com.example.eventMate.databinding.ActivityEnrollBinding
import com.example.eventMate.domains.EventDomain
import com.example.eventMate.progress.BaseActivity
import com.google.gson.Gson

class EnrollAct : BaseActivity() {
    private lateinit var binding : ActivityEnrollBinding
    private  lateinit var eventObj : EventDomain
    private var position : Int = 0
    private lateinit var currentDate : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnrollBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        showProgressBar()
        getCurrentDate()
        val objString = intent.getStringExtra("objString")
        position = intent.getIntExtra("position",0)
        val gson = Gson()
        eventObj = gson.fromJson(objString, EventDomain::class.java)
        binding.cancel.setOnClickListener {
            finish()
        }
        binding.register.setOnClickListener {
            if(eventObj.deadline < currentDate){
                Toast.makeText(this@EnrollAct,"Registration is now closed..",Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(this@EnrollAct, DetailsEnroll::class.java)
                intent.putExtra("EventID",eventObj.key)
                intent.putExtra("currentDate",currentDate)
                startActivity(intent)
            }
        }
        binding.eventTitle.text = eventObj.name
        binding.venue.text = eventObj.venue
        binding.date.text = eventObj.date
        binding.time.text = eventObj.time
        binding.description.text = eventObj.description
        binding.creator.text = eventObj.creator
        val prefix = R.drawable.img_1
        binding.image.setImageResource(prefix+position%7)
    }
    private fun getCurrentDate() {
        val queue = Volley.newRequestQueue(this@EnrollAct)
        val url = "https://timeapi.io/api/Time/current/coordinate?latitude=22.5726&longitude=88.3639"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            {response ->
                currentDate = response.getString("date")
                dismissProgessBar()
            },
            {
                    error ->
                println("Error ${error.message}")
            })
        queue.add(jsonObjectRequest)
    }
}