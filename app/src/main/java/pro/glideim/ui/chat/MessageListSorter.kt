package pro.glideim.ui.chat

import com.dengzii.adapter.SuperAdapter
import pro.glideim.sdk.IMMessage

class MessageListSorter(adapter: SuperAdapter) : SortedListAdapterCallback<IMMessage>(adapter) {

    override fun compare(o1: IMMessage, o2: IMMessage): Int {
        var c1 = o1.mid
        var c2 = o2.mid
        if (c1 == 0L || c2 == 0L) {
            c1 = o1.sendAt
            c2 = o2.sendAt
        }
        if (c1 == c2) {
            return 0
        }
        return (c2 - c1).toInt()
    }

    override fun areContentsTheSame(oldItem: IMMessage, newItem: IMMessage): Boolean {
        return oldItem.sendAt == newItem.sendAt && oldItem.content == newItem.content
    }

    override fun areItemsTheSame(item1: IMMessage, item2: IMMessage): Boolean {
        if (item1.mid == 0L && item2.mid == 0L) {
            return item1.sendAt == item2.sendAt
        }
        return item1.mid == item2.mid
    }

}