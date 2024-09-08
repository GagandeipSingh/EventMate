package com.example.eventMate.activities

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eventMate.R
import com.example.eventMate.databinding.ActivityDashboardBinding
import com.example.eventMate.domains.AlrRegDomain
import com.example.eventMate.domains.UserDomain
import com.example.eventMate.progress.BaseActivity
import com.example.eventMate.scheduler.NotificationReceiver
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.util.Calendar

class DashboardAct : BaseActivity() {
    private lateinit var binding :  ActivityDashboardBinding
    private lateinit var accountType : String
    private var userName  : String? = null
    private lateinit var pendingIntent : PendingIntent
    private lateinit var alarmManager: AlarmManager
    private var list : ArrayList<AlrRegDomain> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),1)
        }
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        alarmManager = getSystemService(AlarmManager::class.java)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> binding.greeting.text = getString(R.string.gud_morning)
            in 12..16 -> binding.greeting.text = getString(R.string.gud_afternoon)
            else -> binding.greeting.text = getString(R.string.gud_evening)
        }
        accountType = intent.getStringExtra("accountType").toString()
        userName = intent.getStringExtra("UserName")
        if(userName == null){
            val usersRef = Firebase.database.getReference("Users").child(Firebase.auth.currentUser!!.uid)
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(UserDomain::class.java)
                    binding.userName.text = userData!!.name
                }

                override fun onCancelled(error: DatabaseError) {} })
        }
        val eventsRef = Firebase.database.getReference("Events")
        showProgressBar()
        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(eventID in snapshot.children){
                    val regRef = eventID.child("Registrations")
                    for(regID in regRef.children){
                        if(regID.child("userId").getValue(String::class.java) == Firebase.auth.currentUser!!.uid){
                            list.add(AlrRegDomain(eventID.child("name").getValue(String :: class.java)!!,
                                regID.child("regDate").getValue(String ::class.java)!!,
                                eventID.child("date").getValue(String :: class.java)!!))
                        }
                    }
                }
                dismissProgessBar()
                if(list.isEmpty()){
                    val sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("send",false)
                    editor.apply()
                }
                else{
                    val sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("send",true)
                    editor.apply()
                }
            }
            override fun onCancelled(error: DatabaseError) {} })

            val sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)
            val send = sharedPreferences.getBoolean("send", false)
            val intentAlarm = Intent(this,NotificationReceiver::class.java)
            pendingIntent = PendingIntent.getBroadcast(this,0,intentAlarm,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            if (send) {
                setDailyAlarm()
            } else {
                cancelAlarm()
            }
        binding.signOut.setOnClickListener {
            showProgressBar()
            if(Firebase.auth.currentUser != null){
                val myRef = Firebase.database.getReference("Users").child(Firebase.auth.currentUser!!.uid)
                val updates = HashMap<String, Any>()
                updates["account"] = ""
                updates["selected"] = ""
                myRef.updateChildren(updates)
                    .addOnSuccessListener {
                        Firebase.auth.signOut()
                        startActivity(Intent(this, LoginAct::class.java))
                        finish()
                        dismissProgessBar()
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("send",false)
                        editor.apply()
                    }
            }
        }
        binding.registered.setOnClickListener {
            val intent = Intent(this@DashboardAct,AlreadyRegAct::class.java)
            startActivity(intent)
        }
        binding.about.setOnClickListener {
            val intent = Intent(this@DashboardAct,AboutAct::class.java)
            startActivity(intent)
        }
        binding.allEvents.setOnClickListener {
            val intent = Intent(this, EventAct::class.java)
            if (accountType == "Organiser") intent.putExtra("accountType", "Organiser")
            else intent.putExtra("accountType", "Participant")
            startActivity(intent)
        }
        binding.byYou.setOnClickListener {
            if(accountType == "Organiser"){
                val intent = Intent(this@DashboardAct, EventAct::class.java)
                intent.putExtra("uploadFlag",1)
                if (accountType == "Organiser") intent.putExtra("accountType", "Organiser")
                else intent.putExtra("accountType", "Participant")
                startActivity(intent)
            }
            else{
                Toast.makeText(this@DashboardAct,"You need to be an organizer to add events.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelAlarm() {
        alarmManager.cancel(pendingIntent)
    }

    private fun setDailyAlarm() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 10)
        }
        val triggerTime = calendar.timeInMillis
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(alarmManager.canScheduleExactAlarms()){
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, AlarmManager.INTERVAL_DAY, pendingIntent)
            }
            else{
                val intentReg = Intent(this,AlreadyRegAct::class.java)
                val pendingIntent = PendingIntent.getActivity(this,4,intentReg,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val notification = NotificationCompat.Builder(applicationContext,"reminder")
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("Reminder")
                    .setContentText("Events await! Check your schedule.")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2,notification)
            }
        }
        else{
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, AlarmManager.INTERVAL_DAY, pendingIntent)
        }
    }
}