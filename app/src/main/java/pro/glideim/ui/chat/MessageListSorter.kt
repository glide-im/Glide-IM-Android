package pro.glideim.ui.chat

import com.dengzii.adapter.SuperAdapter
import pro.glideim.ui.SortedListAdapterCallback
import pro.glideim.ui.chat.viewholder.ChatMessageViewData

class MessageListSorter(adapter: SuperAdapter) : SortedListAdapterCallback<ChatMessageViewData>(adapter) {

    override fun compare(o1: ChatMessageViewData, o2: ChatMessageViewData): Int {
        var c1 = o1.message.mid
        var c2 = o2.message.mid
        if (c1 == 0L || c2 == 0L) {
            c1 = o1.message.sendAt
            c2 = o2.message.sendAt
        }
        if (c1 == c2) {
            return 0
        }
        return (c2 - c1).toInt()
    }

    override fun areContentsTheSame(oldItem: ChatMessageViewData, newItem: ChatMessageViewData): Boolean {
        return oldItem.message.sendAt == newItem.message.sendAt && oldItem.message.content == newItem.message.content
    }

    override fun areItemsTheSame(item1: ChatMessageViewData, item2: ChatMessageViewData): Boolean {
        if (item1.message.mid == 0L && item2.message.mid == 0L) {
            return item1.message.sendAt == item2.message.sendAt
        }
        return item1.message.mid == item2.message.mid
    }

}