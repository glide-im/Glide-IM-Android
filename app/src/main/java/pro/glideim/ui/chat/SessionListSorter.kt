package pro.glideim.ui.chat

import androidx.recyclerview.widget.SortedListAdapterCallback
import com.dengzii.adapter.SuperAdapter
import pro.glideim.sdk.IMSession

class SessionListSorter(adapter: SuperAdapter) : SortedListAdapterCallback<IMSession>(adapter) {

    override fun compare(o1: IMSession, o2: IMSession): Int {
        if (o1.updateAt == o2.updateAt) {
            return 0
        }
        return (o2.updateAt - o1.updateAt).toInt()
    }

    override fun areContentsTheSame(oldItem: IMSession, newItem: IMSession): Boolean {
        return oldItem.updateAt == newItem.updateAt && oldItem.title == newItem.title
    }

    override fun areItemsTheSame(item1: IMSession, item2: IMSession): Boolean {
        return item1.to == item2.to
    }

}