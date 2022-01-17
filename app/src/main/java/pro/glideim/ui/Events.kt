package pro.glideim.ui

import com.blankj.utilcode.util.BusUtils

object Events {
    const val EVENT_UPDATE_CONTACTS = "EVENT_UPDATE_CONTACTS"

    fun updateContacts(){
        BusUtils.post(Events.EVENT_UPDATE_CONTACTS)
    }
}