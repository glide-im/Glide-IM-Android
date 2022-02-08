package pro.glideim.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


fun LifecycleOwner.onState(state: Lifecycle.State, callback: () -> Unit) {
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event.targetState == state) {
                callback.invoke()
                lifecycle.removeObserver(this)
            }
        }
    })
}