package com.example.eventMate.domains

import java.util.Date

data class EventDomain(val name:String="", val description:String="", val creator:String="", val creatorId:String="",
                       val date:String="", val time:String="", val deadline:String="", val selectedDate: Date = Date(), val venue:String="", val timestamp: Long = 0L, val key : String="")
