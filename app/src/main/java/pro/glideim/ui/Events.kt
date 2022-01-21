package pro.glideim.ui

import pro.glideim.utils.MyBusUtils

object Events {
    const val EVENT_UPDATE_CONTACTS = "EVENT_UPDATE_CONTACTS"

    fun updateContacts() {
        MyBusUtils.post(EVENT_UPDATE_CONTACTS)
    }
}