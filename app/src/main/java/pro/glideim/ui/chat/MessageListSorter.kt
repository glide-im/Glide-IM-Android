package pro.glideim.ui.chat

import androidx.recyclerview.widget.SortedListAdapterCallback
import com.dengzii.adapter.SuperAdapter
import pro.glideim.sdk.entity.IMMessage

class MessageListSorter(adapter: SuperAdapter) : SortedListAdapterCallback<IMMessage>(adapter) {

    override fun compare(o1: IMMessage, o2: IMMessage): Int {
        if (o1.mid == o2.mid) {
            return 0
        }
        return (o1.mid - o2.mid).toInt()
    }

    override fun areContentsTheSame(oldItem: IMMessage, newItem: IMMessage): Boolean {
        return oldItem.mid == newItem.mid && oldItem.state == newItem.state && oldItem.type == newItem.type
    }

    override fun areItemsTheSame(item1: IMMessage, item2: IMMessage): Boolean {
        return item1.mid == item2.mid
    }

}