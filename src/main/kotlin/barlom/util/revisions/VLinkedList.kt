//
// (C) Copyright 2014-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

//---------------------------------------------------------------------------------------------------------------------

/**
 * A version-managing handle to a linked list with transactional revisions.
 *
 * @param <T> the type of the value that is held in the list.
 */
class VLinkedList<T : Any> {

    /**
     * Versioned reference to the head of the list. Links through the list are themselves versioned as well.
     */
    private val _firstLink: V<Link<T>?> = V(null)

    /**
     * The (versioned) size of this list.
     */
    private val _size = V(0)


    /** The size of this list for the active revision in review. Size is O(1). */
    val size: Int
        get() = _size.get()


    /**
     * Adds an item to the head of the list. Insertion is O(1)
     */
    fun add(value: T) {
        val link = Link(value, V(_firstLink.get()))
        _firstLink.set(link)
        _size.increment()
    }

    /**
     * Makes a snapshot copy of the linked list. Copying is O(n)
     */
    fun asList(): List<T> = asMutableList()

    /**
     * Makes a snapshot copy of the linked list. Copying is O(n)
     */
    private fun asMutableList(): MutableList<T> {

        val result = ArrayList<T>(size)

        var link = _firstLink.get()
        while (link != null) {
            result.add(link.value)
            link = link.nextLink.get()
        }

        return result

    }

    /**
     * Empties the list. O(1).
     */
    fun clear() {
        _firstLink.set(null)
        _size.set(0)
    }

    /**
     * Tests whether the list contains a given value. Search is O(n).
     */
    fun contains(value: T): Boolean {

        var link = _firstLink.get()
        while (link != null) {

            if (link.value == value) {
                return true
            }

            link = link.nextLink.get()

        }

        return false

    }

    /**
     * Tests whether the list contains a value satisfying the given condition. Search is O(n).
     */
    fun contains(predicate: (T) -> Boolean): Boolean {

        var link = _firstLink.get()
        while (link != null) {

            if (predicate(link.value)) {
                return true
            }

            link = link.nextLink.get()

        }

        return false

    }

    /**
     * Finds the first value satisfying the given condition. Search is O(n).
     * @return the value found or else null
     */
    fun find(predicate: (T) -> Boolean): T? {

        var link = _firstLink.get()
        while (link != null) {

            if (predicate(link.value)) {
                return link.value
            }

            link = link.nextLink.get()

        }

        return null

    }

    /**
     * Performs the given [action] on each item in the list.
     */
    fun forEach(action: (T) -> Unit) {

        var link = _firstLink.get()
        while (link != null) {
            val nextLink = link.nextLink.get()
            action(link.value)
            link = nextLink
        }

    }

    /**
     * Performs the given [action] on each item in the list. Quits early if action() returns false.
     */
    fun forEachWhile(action: (T) -> Boolean) {

        var link = _firstLink.get()
        while (link != null) {

            val nextLink = link.nextLink.get()

            if (!action(link.value)) {
                break
            }

            link = nextLink

        }

    }

    /**
     * Returns the element at the specified [index] in the list. O(n)
     */
    operator fun get(index: Int): T {

        if (index < 0) {
            throw IndexOutOfBoundsException()
        }

        var i = 0
        var link = _firstLink.get()

        while (i < index && link != null) {

            i += 1
            link = link.nextLink.get()

        }

        if (link == null) {
            throw IndexOutOfBoundsException()
        }

        return link.value

    }

    /**
     * Returns the index of the first occurrence of the specified element in the list, or -1 if the specified
     * element is not contained in the list. O(n)
     */
    fun indexOf(value: T): Int {

        var result = 0
        var link = _firstLink.get()

        while (link != null) {

            if (link.value == value) {
                return result
            }

            link = link.nextLink.get()
            result += 1

        }

        return -1

    }

    /** Whether the list is empty. */
    fun isEmpty(): Boolean = _size.get() == 0

    /** Whether the list is non-empty. */
    fun isNotEmpty(): Boolean = _size.get() > 0

    /** Returns an iterator over the items in the list. */
    operator fun iterator(): Iterator<T> = VLinkedListIterator(_firstLink)

    /**
     * Returns the index of the last occurrence of the specified element in the list, or -1 if the specified
     * [value] is not contained in the list. O(n)
     */
    fun lastIndexOf(value: T): Int {

        var result = -1
        var index = 0
        var link = _firstLink.get()

        while (link != null) {

            if (link.value == value) {
                result = index
            }

            link = link.nextLink.get()
            index += 1

        }

        return result

    }

    /**
     * Returns a list containing the results of applying the given [transform] function
     * to each element in this linked list.
     */
    fun <R> map(transform: (T) -> R): List<R> {

        val result = ArrayList<R>(size)

        var link = _firstLink.get()
        while (link != null) {
            result.add(transform(link.value))
            link = link.nextLink.get()
        }

        return result

    }

    /**
     * Removes an item (the first matching the input) from the list. Removal is O(n).
     */
    fun remove(value: T): Boolean {

        if (size == 0) {
            return false
        }

        var link = _firstLink.get()!!

        if (link.value == value) {
            _firstLink.set(link.nextLink.get())
            _size.decrement()
            return true
        }

        var nextLink = link.nextLink.get()

        while (nextLink != null) {

            if (nextLink.value == value) {
                link.nextLink.set(nextLink.nextLink.get())
                _size.decrement()
                return true
            }

            link = nextLink
            nextLink = link.nextLink.get()

        }

        return false

    }

    /**
     * Removes an element at the specified [index] from the list. O(n)
     *
     * @return the element that has been removed.
     */
    fun removeAt(index: Int): T {

        if (index >= size || index < 0) {
            throw IndexOutOfBoundsException()
        }

        var link = _firstLink.get()!!

        if (index == 0) {
            _firstLink.set(link.nextLink.get())
            _size.decrement()
            return link.value
        }

        var nextLink = link.nextLink.get()!!

        var i = 0
        while (i < index - 1) {
            link = nextLink
            nextLink = link.nextLink.get()!!
            i += 1
        }

        link.nextLink.set(nextLink.nextLink.get())

        _size.decrement()

        return nextLink.value

    }

    /**
     * Replaces the element at the specified position in this list with the specified element. O(n)
     *
     * @return the element previously at the specified position.
     */
    operator fun set(index: Int, value: T): T {

        if (index >= size || index < 0) {
            throw IndexOutOfBoundsException()
        }

        var link = _firstLink.get()!!

        if (index == 0) {
            _firstLink.set(Link(value, link.nextLink))
            return link.value
        }

        var nextLink = link.nextLink.get()!!

        var i = 0
        while (i < index - 1) {
            link = nextLink
            nextLink = link.nextLink.get()!!
            i += 1
        }

        link.nextLink.set(Link(value, nextLink.nextLink))

        return nextLink.value

    }

    /**
     * Makes a snapshot copy of the linked list then sorts it by the given criteria. Copying is O(n); sort is ~ O(n log(n))
     */
    fun <R : Comparable<R>> sortedBy(selector: (T) -> R?): List<T> {
        val result = asMutableList()
        result.sortBy(selector)
        return result
    }


    /**
     * Internal record structure for links in the list, each versioned.
     *
     * @param <T> the type of the value that is managed through its revisions.
     */
    private class Link<T>(
        val value: T,
        val nextLink: V<Link<T>?>
    )

    private class VLinkedListIterator<out T>(
        private var link: V<Link<T>?>
    ) : Iterator<T> {

        override fun hasNext(): Boolean = link.get() != null

        override fun next(): T {
            val curr = link.get() ?: throw IndexOutOfBoundsException()
            val result = curr.value
            link = curr.nextLink
            return result
        }

    }

}

//---------------------------------------------------------------------------------------------------------------------

