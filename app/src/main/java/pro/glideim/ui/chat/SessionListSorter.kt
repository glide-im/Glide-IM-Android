package pro.glideim.ui.chat

import com.dengzii.adapter.SuperAdapter

class SessionListSorter(adapter: SuperAdapter) :
    SortedListAdapterCallback<SessionViewData>(adapter) {

    override fun compare(o1: SessionViewData, o2: SessionViewData): Int {
        if (o1.updateAt == o2.updateAt) {
            return 0
        }
        return (o2.updateAt - o1.updateAt).toInt()
    }

    override fun areContentsTheSame(oldItem: SessionViewData, newItem: SessionViewData): Boolean {
        return oldItem.updateAt == newItem.updateAt &&
                oldItem.title == newItem.title &&
                oldItem.unread == newItem.unread &&
                oldItem.avatar == newItem.avatar
    }

    override fun areItemsTheSame(item1: SessionViewData, item2: SessionViewData): Boolean {
        return item1.to == item2.to && item1.type == item2.type
    }
}