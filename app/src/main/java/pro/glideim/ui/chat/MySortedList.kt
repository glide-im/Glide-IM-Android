package pro.glideim.ui.chat

import androidx.recyclerview.widget.SortedList

class MySortedList<T> : List<T> {

    lateinit var l: SortedList<T>

    fun remove(i: T): Boolean {
        return l.remove(i)
    }

    fun add(m: T) {
        l.add(m)
    }

    fun clear() {
        l.clear()
    }

    fun addAll(ms: List<T>) {
        l.addAll(ms)
    }

    override val size get() = l.size()

    override fun contains(element: T): Boolean {
        return l.indexOf(element) != SortedList.INVALID_POSITION
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        for (element in elements) {
            if (!contains(element)) {
                return false
            }
        }
        return true
    }

    override fun get(index: Int): T {
        return l.get(index)
    }

    override fun indexOf(element: T): Int {
        return l.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return l.size() == 0
    }

    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            override fun hasNext(): Boolean {
                return false
            }

            override fun next(): T {
                return get(0)
            }
        }
    }

    override fun lastIndexOf(element: T): Int {
        return l.indexOf(element)
    }

    override fun listIterator(): ListIterator<T> {
        return object : ListIterator<T> {
            override fun hasNext() = false
            override fun hasPrevious() = false
            override fun next() = get(0)
            override fun nextIndex() = 0
            override fun previous() = get(0)
            override fun previousIndex() = 0
        }
    }

    override fun listIterator(index: Int): ListIterator<T> {
        return listIterator()
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        return emptyList()
    }
}