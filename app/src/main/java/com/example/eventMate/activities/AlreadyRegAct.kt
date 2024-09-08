package com.example.eventMate.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventMate.R
import com.example.eventMate.adapters.RegEventAdapter
import com.example.eventMate.databinding.ActivityAlreadyRegBinding
import com.example.eventMate.databinding.FilterDialogLayoutBinding
import com.example.eventMate.domains.AlrRegDomain
import com.example.eventMate.progress.BaseActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class AlreadyRegAct : BaseActivity() {
    private lateinit var binding : ActivityAlreadyRegBinding
    private lateinit var list : ArrayList<AlrRegDomain>
    private lateinit var adapter : RegEventAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAlreadyRegBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        list = arrayListOf()
        adapter = RegEventAdapter(list,this@AlreadyRegAct)
        binding.Events.adapter = adapter
        binding.Events.layoutManager = LinearLayoutManager(this@AlreadyRegAct)
        val eventsRef = Firebase.database.getReference("Events")
        showProgressBar()
        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
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
                }
                dismissProgessBar()
                adapter.notifyDataSetChanged()
                refreshEvents()
            }
            override fun onCancelled(error: DatabaseError) {} })

        binding.filter.setOnClickListener {
            val dialog = Dialog(this)
            val dialogBinding = FilterDialogLayoutBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)
            dialogBinding.sortReg.setOnClickListener {
                val tempList = adapter.getList()
                tempList.sortWith(descendingComparatorReg)
                val tempAdapter = RegEventAdapter(tempList, this)
                binding.Events.adapter = tempAdapter
                dialog.dismiss()
            }
            dialogBinding.sortAdd.setOnClickListener {
                val tempList = adapter.getList()
                tempList.sortWith(descendingComparator)
                val tempAdapter = RegEventAdapter(tempList, this)
                binding.Events.adapter = tempAdapter
                dialog.dismiss()
            }
            dialog.show()
            // Set dialog window attributes for full width and bottom positioning
            val window = dialog.window
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window?.attributes?.gravity = Gravity.BOTTOM

            // Set background drawable for customization
            window?.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.dialog_bg_reg))
        }
    }
    private val descendingComparatorReg = Comparator<AlrRegDomain> { event1, event2 ->
        event2.regDate.compareTo(event1.regDate)
    }
    private val descendingComparator = Comparator<AlrRegDomain> { event1, event2 ->
        event1.date.compareTo(event2.date)
    }
    private fun refreshEvents() {
        if(adapter.itemCount > 0){
            binding.emptyImg.visibility = View.INVISIBLE
            binding.emptyLine.visibility = View.INVISIBLE
        }
        else{
            binding.emptyImg.visibility = View.VISIBLE
            binding.emptyLine.visibility = View.VISIBLE
        }
    }
}