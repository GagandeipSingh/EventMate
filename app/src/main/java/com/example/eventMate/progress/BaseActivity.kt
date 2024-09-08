package com.example.eventMate.progress

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eventMate.R

open class BaseActivity : AppCompatActivity() {
    private lateinit var pb : Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun showProgressBar(){
        pb = Dialog(this)
        pb.setContentView(R.layout.activity_progress_bar)
        pb.setCancelable(false)
        pb.show()
        val window = pb.window
        window?.let {
            val params = it.attributes
            params.width = 340
            params.height = 340
            it.attributes = params
        }
    }

    fun dismissProgessBar(){
        pb.dismiss()
    }

    fun showToast(activity: Activity, msg:String){
        Toast.makeText(activity,msg,Toast.LENGTH_SHORT).show()
    }
}