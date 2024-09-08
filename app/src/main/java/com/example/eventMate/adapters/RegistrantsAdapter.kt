package com.example.eventMate.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventMate.R
import com.example.eventMate.databinding.RegViewholderBinding
import com.example.eventMate.domains.RegDetailsDomain

class RegistrantsAdapter(private val list : ArrayList<RegDetailsDomain>, private val context : Context):RecyclerView.Adapter<RegistrantsAdapter.ViewHolder>() {
    class ViewHolder(val binding : RegViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val regBinding = RegViewholderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(regBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.userName.text = list[position].name
        holder.binding.Class.text = list[position].classValue
        holder.binding.department.text = list[position].department
        holder.binding.email.text = String.format(context.getString(R.string.formattedEmail),list[position].email)
        val prefix = R.color.color_1
        holder.binding.band.setBackgroundColor(context.getColor(prefix+position%7))
    }

    fun getList() : ArrayList<RegDetailsDomain>{
        return list
    }
}