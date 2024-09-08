package com.example.eventMate.interfaces

import android.view.View
import com.example.eventMate.domains.EventDomain

interface InteractionInterFace {
    fun showMenu(view: View, position : Int,list : ArrayList<EventDomain>)
}