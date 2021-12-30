package pro.glideim.ui.chat

import androidx.recyclerview.widget.SortedList
import pro.glideim.sdk.IMMessage

class MySortedList : List<IMMessage> {

    lateinit var l: SortedList<IMMessage>

    fun add(m: IMMessage) {
        l.add(m)
    }

    fun clear() {
        l.clear()
    }

    fun addAll(ms: List<IMMessage>) {
        l.addAll(ms)
    }

    override val size get() = l.size()

    override fun contains(element: IMMessage): Boolean {
        return l.indexOf(element) != SortedList.INVALID_POSITION
    }

    override fun containsAll(elements: Collection<IMMessage>): Boolean {
        for (element in elements) {
            if (!contains(element)) {
                return false
            }
        }
        return true
    }

    override fun get(index: Int): IMMessage {
        return l.get(index)
    }

    override fun indexOf(element: IMMessage): Int {
        return l.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return l.size() == 0
    }

    override fun iterator(): Iterator<IMMessage> {
        return object : Iterator<IMMessage> {
            override fun hasNext(): Boolean {
                return false
            }

            override fun next(): IMMessage {
                return get(0)
            }
        }
    }

    override fun lastIndexOf(element: IMMessage): Int {
        return l.indexOf(element)
    }

    override fun listIterator(): ListIterator<IMMessage> {
        return object : ListIterator<IMMessage> {
            override fun hasNext() = false
            override fun hasPrevious() = false
            override fun next() = get(0)
            override fun nextIndex() = 0
            override fun previous() = get(0)
            override fun previousIndex() = 0
        }
    }

    override fun listIterator(index: Int): ListIterator<IMMessage> {
        return listIterator()
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<IMMessage> {
        return emptyList()
    }
}