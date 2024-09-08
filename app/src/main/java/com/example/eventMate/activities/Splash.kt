package com.example.eventMate.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eventMate.R
import com.example.eventMate.databinding.ActivitySplashBinding
import com.example.eventMate.domains.UserDomain
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var selected : String
    private lateinit var accountType : String
    private lateinit var userName : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add this line to disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = Firebase.auth
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        showContentWithAnimation()
        check()
        binding.btnStart.setOnClickListener {
            if(auth.currentUser == null){
                startActivity(Intent(this, LoginAct::class.java))
                binding.loadingAnim.visibility = View.INVISIBLE
                finish()
            }
            else if(selected != "1"){
                startActivity(Intent(this@Splash, UserType::class.java))
                finish()
            }
            else{
                val intent = Intent(this@Splash, DashboardAct::class.java)
                intent.putExtra("accountType",accountType)
                intent.putExtra("userName",userName)
                startActivity(intent)
                finish()
            }
        }
        }

    private fun check() {
        val auth  = Firebase.auth
        val userId = auth.currentUser?.uid
        selected = "0"
        if (userId != null) {
            val database = Firebase.database
            val myRef = database.getReference("Users").child(userId)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userDomainData = dataSnapshot.getValue(UserDomain::class.java)
                    selected = userDomainData!!.selected
                    userName = userDomainData.name
                    accountType = userDomainData.account
                    showContentWithAnimationBtn()
                }
                override fun onCancelled(databaseError: DatabaseError) {} })
        }
        else{
            Handler(Looper.getMainLooper()).postDelayed({
                showContentWithAnimationBtn()
            }, 1600)
        }
    }
    private fun showContentWithAnimationBtn() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1600L // Adjust duration for desired animation speed
            interpolator = AccelerateDecelerateInterpolator() // Adjust interpolator for animation style
        }
        binding.btnStart.startAnimation(fadeIn)
        binding.btnStart.visibility = View.VISIBLE
        binding.btnAnim.startAnimation(fadeIn)
        binding.btnAnim.visibility = View.VISIBLE
        binding.loadingAnim.visibility = View.INVISIBLE
    }
    private fun showContentWithAnimationMain() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1200L // Adjust duration for desired animation speed
            interpolator = AccelerateDecelerateInterpolator() // Adjust interpolator for animation style
        }
        // Start the animation on the view
        binding.animationView.startAnimation(fadeIn)
        binding.animationView.visibility = View.VISIBLE
    }
    private fun showContentWithAnimation() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 800L // Adjust duration for desired animation speed
            interpolator = AccelerateDecelerateInterpolator() // Adjust interpolator for animation style
        }
        // Start the animation on the view
        binding.imageView3.startAnimation(fadeIn)
        binding.imageView3.visibility = View.VISIBLE
        binding.imageView4.startAnimation(fadeIn)
        binding.imageView4.visibility = View.VISIBLE
        binding.loadingAnim.startAnimation(fadeIn)
        binding.loadingAnim.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            showContentWithAnimationMain()
        }, 800)
    }
}