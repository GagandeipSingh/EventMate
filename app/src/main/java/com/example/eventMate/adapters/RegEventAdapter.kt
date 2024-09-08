package com.example.eventMate.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventMate.R
import com.example.eventMate.databinding.EventViewholderBinding
import com.example.eventMate.domains.AlrRegDomain

class RegEventAdapter (private val list : ArrayList<AlrRegDomain>, private val context : Context) : RecyclerView.Adapter<RegEventAdapter.ViewHolder>(){
    class ViewHolder(val binding : EventViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val regBinding = EventViewholderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(regBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prefix = R.drawable.img_1
        holder.binding.ImageBehind.setImageResource(prefix+position%7)
        holder.binding.lastDateTxt.text = context.getString(R.string.eventDate)
        holder.binding.lastDate.text = list[position].date
        holder.binding.moreOptions.visibility = View.INVISIBLE
        holder.binding.eventTitle.text = list[position].eventName
        holder.binding.posted.text = context.getString(R.string.reg_on)
        val dateString = list[position].regDate
        val formattedDateString = dateString.substring(0, 2) + " / " + dateString.substring(3, 5) + " / " + dateString.substring(8, 10)
        holder.binding.creator.text = formattedDateString
    }
    fun getList() : ArrayList<AlrRegDomain>{
        return list
    }
}