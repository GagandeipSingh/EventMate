package com.example.eventMate.scheduler

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.eventMate.R
import com.example.eventMate.activities.AlreadyRegAct


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val sharedPrefs = context?.getSharedPreferences("my_prefs",Context.MODE_PRIVATE)
        val send = sharedPrefs?.getBoolean("send",false) ?: false
        if(send){
            val intentReg = Intent(context,AlreadyRegAct::class.java)
            val pendingIntent = PendingIntent.getActivity(context,4,intentReg,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(context?.applicationContext?:return,"reminder")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Reminder")
                .setContentText("Events await! Check your schedule.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(2,notification)
        }
    }
}