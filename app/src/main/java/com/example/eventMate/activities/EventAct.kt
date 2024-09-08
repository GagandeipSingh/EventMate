package com.example.eventMate.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventMate.R
import com.example.eventMate.adapters.EventAdapter
import com.example.eventMate.databinding.ActivityEventBinding
import com.example.eventMate.databinding.DelBgBinding
import com.example.eventMate.domains.EventDomain
import com.example.eventMate.interfaces.InteractionInterFace
import com.example.eventMate.progress.BaseActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.gson.Gson

class EventAct : BaseActivity(), InteractionInterFace {
    private lateinit var binding : ActivityEventBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var userID : String
    private lateinit var eventsList : ArrayList<EventDomain>
    private lateinit var tempList : ArrayList<EventDomain>
    private lateinit var eventAdapter : EventAdapter
    private lateinit var accountType : String
    private var uploadFlag : Int = 0
    private var sortFlag : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        auth = Firebase.auth
        eventsList = arrayListOf()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        accountType = intent.getStringExtra("accountType").toString()
        uploadFlag = intent.getIntExtra("uploadFlag",0)
        if(accountType == "Participant"){
            binding.addLine.text = null
            binding.addFloat.visibility = View.INVISIBLE
        }
        binding.addFloat.setOnClickListener {
            val intent = Intent(this@EventAct, EventAdd::class.java)
            startActivity(intent)
        }
        binding.options.setOnClickListener {
            showMenuMain(it)
        }
        eventAdapter = EventAdapter(eventsList, this,Firebase.auth.currentUser!!.uid, this@EventAct, accountType)
        binding.Events.adapter = eventAdapter
        binding.Events.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        retrieveEvents()
    }

    private fun retrieveEvents() {
        showProgressBar()
        val userId = auth.currentUser?.uid
        userID = userId.toString()
        if(userId != null){
            val database = Firebase.database
            val ref = database.getReference("Events")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    eventsList.clear()
                    for(eventSnapshot in snapshot.children){
                        val eventData = eventSnapshot.getValue(EventDomain::class.java)
                        eventsList.add(
                            EventDomain(eventData!!.name,eventData.description,eventData.creator,
                            eventData.creatorId,eventData.date,eventData.time,eventData.deadline,eventData.selectedDate,eventData.venue,
                            eventData.timestamp,eventSnapshot.key.toString())
                        )
                    }
                    if(uploadFlag == 1) filterByYou()
                    if(sortFlag == "Ascending") sortAsc()
                    if(sortFlag == "DeadLine") sortDeadLine()
                    eventAdapter.notifyDataSetChanged()
                    refreshEvents()
                    dismissProgessBar()
                }
                override fun onCancelled(error: DatabaseError) {} })
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun showMenuMain(view : View) {
        val optionsMain = PopupMenu(this,view,Gravity.END)
        optionsMain.inflate(R.menu.options_main)
        optionsMain.show()
        if(accountType == "Participant"){
            optionsMain.menu.findItem(R.id.up_by_u).setVisible(false)
            optionsMain.menu.findItem(R.id.all_uploads).setVisible(false)
        }
        val byYou = optionsMain.menu.findItem(R.id.up_by_u)
        val allUploads = optionsMain.menu.findItem(R.id.all_uploads)
        if(uploadFlag == 0 && accountType!="Participant"){
            byYou.setVisible(true)
            allUploads.setVisible(false)
        }
        else if(uploadFlag == 1 && accountType!="Participant"){
            byYou.setVisible(false)
            allUploads.setVisible(true)
        }
        byYou.setOnMenuItemClickListener {
            if(uploadFlag == 0 && accountType!="Participant"){
                filterByYou()
            }
            return@setOnMenuItemClickListener true
        }
        allUploads.setOnMenuItemClickListener {
            if(uploadFlag == 1){
                uploadFlag = 0
                eventAdapter = EventAdapter(eventsList, this,Firebase.auth.currentUser!!.uid, this@EventAct, accountType)
                binding.Events.adapter = eventAdapter
                refreshEvents()
            }
            return@setOnMenuItemClickListener true
        }
        optionsMain.menu.findItem(R.id.sign_out).setOnMenuItemClickListener {
            showProgressBar()
            if(auth.currentUser != null){
                val myRef = Firebase.database.getReference("Users").child(userID)
                val updates = HashMap<String, Any>()
                updates["account"] = ""
                updates["selected"] = ""
                myRef.updateChildren(updates)
                    .addOnSuccessListener {
                        auth.signOut()
                        startActivity(Intent(this, LoginAct::class.java))
                        finish()
                        dismissProgessBar()
                        val sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("send",false)
                        editor.apply()
                    }
            }
            return@setOnMenuItemClickListener true
        }

        optionsMain.menu.findItem(R.id.sort_asc).setOnMenuItemClickListener {
            sortAsc()
            sortFlag = "Ascending"
            return@setOnMenuItemClickListener true
        }

        optionsMain.menu.findItem(R.id.sort_deadLine).setOnMenuItemClickListener {
            sortDeadLine()
            sortFlag = "DeadLine"
            return@setOnMenuItemClickListener true
        }

        optionsMain.menu.findItem(R.id.about).setOnMenuItemClickListener{
            val intent = Intent(this@EventAct,AboutAct::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
    }

    private fun sortDeadLine() {
        val tempList = eventAdapter.getList()
        tempList.sortWith(deadLineComparator)
        val tempAdapter = EventAdapter(tempList, this,Firebase.auth.currentUser!!.uid, this@EventAct, accountType)
        binding.Events.adapter = tempAdapter
    }

    private val ascendingComparator = Comparator<EventDomain> { event1, event2 ->
        event2.timestamp.compareTo(event1.timestamp)
    }

    private val deadLineComparator = Comparator<EventDomain> { event1, event2 ->
        event1.deadline.compareTo(event2.deadline)
    }

    private fun sortAsc() {
        val tempList = eventAdapter.getList()
        tempList.sortWith(ascendingComparator)
        val tempAdapter = EventAdapter(tempList, this,Firebase.auth.currentUser!!.uid, this@EventAct, accountType)
        binding.Events.adapter = tempAdapter
    }

    private fun filterByYou() {
            uploadFlag = 1
            tempList = eventsList.filter { it.creatorId == userID } as ArrayList<EventDomain>
            val tempAdapter = EventAdapter(tempList, this,Firebase.auth.currentUser!!.uid, this@EventAct, accountType)
            binding.Events.adapter = tempAdapter
            refreshEventsTemp(tempAdapter)
    }

    private fun refreshEventsTemp(tempAdapter: EventAdapter) {
        if(tempAdapter.itemCount > 0){
            binding.emptyImg.visibility = View.GONE
            binding.emptyLine.visibility = View.GONE
            binding.addLine.visibility = View.GONE
        }
        else{
            binding.emptyImg.visibility = View.VISIBLE
            binding.emptyLine.visibility = View.VISIBLE
            binding.addLine.visibility = View.VISIBLE
        }
    }

    private fun refreshEvents() {
        if(eventAdapter.itemCount > 0){
            binding.emptyImg.visibility = View.GONE
            binding.emptyLine.visibility = View.GONE
            binding.addLine.visibility = View.GONE
        }
        else{
            binding.emptyImg.visibility = View.VISIBLE
            binding.emptyLine.visibility = View.VISIBLE
            binding.addLine.visibility = View.VISIBLE
        }
    }

    override fun showMenu(view: View, position : Int, list : ArrayList<EventDomain>) {
        val optionsEvents = PopupMenu(this,view, Gravity.END)
        optionsEvents.inflate(R.menu.options_events)
        optionsEvents.show()
        optionsEvents.menu.findItem(R.id.editEvent).setOnMenuItemClickListener {
            editEvent(position,list)
            return@setOnMenuItemClickListener true
        }
        optionsEvents.menu.findItem(R.id.dropEvent).setOnMenuItemClickListener {
            deleteEvent(position,list)
            return@setOnMenuItemClickListener true
        }
    }

    private fun editEvent(position: Int,list : ArrayList<EventDomain>) {
        val intent = Intent(this@EventAct, EventAdd::class.java)
        val gson = Gson()
        val objString = gson.toJson(list[position])
        intent.putExtra("objString",objString)
        intent.putExtra("edit","EditEvent")
        startActivity(intent)
    }

    private fun deleteEvent(position: Int,list : ArrayList<EventDomain>) {
        val dialog = Dialog(this@EventAct)
        val dialogBinding = DelBgBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        // Set dialog window attributes for full width and bottom positioning
        val window = dialog.window
        window?.let {
            val params = it.attributes
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = params
        }
        // Set background drawable for customization
        window?.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.dialog_bg))
        dialog.show()
        dialog.setCancelable(false)
        dialogBinding.deleteBtn.setOnClickListener {
            Firebase.database.getReference("Events").child(list[position].key).removeValue()
                .addOnCompleteListener {
                    dialog.dismiss()
                    Toast.makeText(this@EventAct,"Event is Dropped..",Toast.LENGTH_SHORT).show()
                    retrieveEvents()
                }
        }
        dialogBinding.cancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}