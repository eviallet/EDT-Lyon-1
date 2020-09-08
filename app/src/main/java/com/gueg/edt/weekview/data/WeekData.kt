package com.gueg.edt.weekview.data

class WeekData {

    private val singleEvents: MutableList<Event.Single> = mutableListOf()

    private val allDays: MutableList<Event.AllDay> = mutableListOf()

    fun add(item: Event.AllDay) {
        allDays.add(item)
    }

    fun add(item: Event.Single) {
        singleEvents.add(item)
    }

    fun getSingleEvents(): List<Event.Single> = singleEvents.toList()

    fun getAllDayEvents(): List<Event.AllDay> = allDays.toList()

    fun isEmpty() = singleEvents.isEmpty() && allDays.isEmpty()

    fun clear() {
        singleEvents.clear()
        allDays.clear()
    }
}