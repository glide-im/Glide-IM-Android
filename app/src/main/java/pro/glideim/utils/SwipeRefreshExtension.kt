package pro.glideim.utils

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import pro.glideim.R


var SwipeRefreshLayout.onRefreshCallback: (() -> Unit)?
    set(value) {
        setTag(R.id.tag_swipe_refresh_on_refresh_listener, value)
    }
    get() {
        return getTag(R.id.tag_swipe_refresh_on_refresh_listener) as? () -> Unit
    }

fun SwipeRefreshLayout.onRefresh(callback: () -> Unit) {
    setOnRefreshListener {
        callback.invoke()
    }
    onRefreshCallback = callback
}

fun SwipeRefreshLayout.startRefresh() {
    isRefreshing = true
    onRefreshCallback?.invoke()
}

fun SwipeRefreshLayout.finishRefresh() {
    isRefreshing = false
}