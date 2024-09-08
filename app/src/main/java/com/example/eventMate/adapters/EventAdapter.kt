package com.example.eventMate.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventMate.interfaces.InteractionInterFace
import com.example.eventMate.R
import com.example.eventMate.activities.EnrollAct
import com.example.eventMate.activities.RegParticipants
import com.example.eventMate.databinding.EventViewholderBinding
import com.example.eventMate.domains.EventDomain
import com.google.gson.Gson

class EventAdapter(private val list:ArrayList<EventDomain>, private val interactionInterFace: InteractionInterFace,
                   private val userId : String, private val context : Context,
                   private val accountType : String):RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    class ViewHolder(val binding : EventViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = EventViewholderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(userId != list[position].creatorId || accountType != "Organiser"){
            holder.binding.moreOptions.visibility = View.GONE
        }
        val prefix = R.drawable.img_1
        holder.binding.ImageBehind.setImageResource(prefix+position%7)
        holder.binding.eventTitle.text = list[position].name
        holder.binding.creator.text = list[position].creator
        holder.binding.lastDate.text = list[position].deadline
        holder.binding.moreOptions.setOnClickListener {
            interactionInterFace.showMenu(it,position,list)
        }
        holder.itemView.setOnClickListener {
            if(accountType == "Participant"){
                val intent = Intent(context, EnrollAct::class.java)
                val gson = Gson()
                val objString = gson.toJson(list[position])
                intent.putExtra("objString",objString)
                intent.putExtra("position",position)
                context.startActivity(intent)
            }
            else{
                val intent = Intent(context, RegParticipants::class.java)
                val gson = Gson()
                val objString = gson.toJson(list[position])
                intent.putExtra("objString",objString)
                intent.putExtra("position",position)
                context.startActivity(intent)
            }
        }
    }
    fun getList() : ArrayList<EventDomain>{
        return list
    }
}