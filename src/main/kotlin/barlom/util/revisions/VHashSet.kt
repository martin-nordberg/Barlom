//
// (C) Copyright 2014-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

//---------------------------------------------------------------------------------------------------------------------

/**
 * A version-managing handle to a hash set with transactional revisions.
 *
 * @param <T> the type of the value that is held in the set - must have a viable hashCode() function.
 */
open class VHashSet<T : Any>(

    nominalTableSize: Int = 1000

) {

    /**
     * The total number of entries in the set.
     */
    private var _size = V(0)

    /**
     * The size of the table of linked lists of entries.
     */
    private val _tableSize =
        if (nominalTableSize <= 127) 127
        else if (nominalTableSize <= 257) 257
        else if (nominalTableSize <= 509) 509
        else if (nominalTableSize <= 1021) 1021
        else if (nominalTableSize <= 2053) 2053
        else if (nominalTableSize <= 4099) 4099
        else 8191
    // TODO: bigger sizes as needed ...

    /**
     * Array of lists of entries, to be indexed by hash code
     */
    private val _table = Array(_tableSize) { VLinkedList<T>() }


    /** The size of this set for the active revision in review. Size is O(1). */
    val size: Int
        get() = _size.get()


    /**
     * Adds an item to the set. Insertion is O(size/tableSize).
     */
    fun add(value: T) {

        val index = getTableIndex(value)

        if (!_table[index].contains(value)) {
            _table[index].add(value)
            _size.increment()
        }

    }

    /**
     * Makes a snapshot copy of the set. Copying is O(n)
     */
    fun asSet(): Set<T> {

        val result = HashSet<T>(size)

        for (list in _table) {

            for (entry in list) {
                result.add(entry)
            }

            list.asList().listIterator()

        }

        return result

    }

    /**
     * Empties the set. O(tableSize).
     */
    fun clear() {

        for (i in 0 until _tableSize) {
            _table[i].clear()
        }

        _size.set(0)

    }

    /**
     * Tests whether the list contains a given value. Search is O(size/tableSize).
     */
    fun contains(value: T): Boolean {

        val index = getTableIndex(value)

        return _table[index].contains(value)

    }

    /**
     * Performs the given [action] on each item in the set.
     * Note that the order of iteration is unlikely to be meaningful since it is based on hash code modulo table size
     * and then by reverse order of insertion for the same hashcode modulo table size.
     */
    fun forEach(action: (T) -> Unit) {

        for (list in _table) {

            for (entry in list) {
                action(entry)
            }

        }

    }

    /** Whether the list is empty. O(1) */
    fun isEmpty(): Boolean = size == 0

    /** Whether the list is non-empty. O(1) */
    fun isNotEmpty(): Boolean = size > 0

    /**
     * Returns a list containing the results of applying the given [transform] function
     * to each element in this linked list.
     */
    fun <R> map(transform: (T) -> R): Set<R> {

        val result = HashSet<R>(size)

        for (list in _table) {

            for (entry in list) {
                result.add(transform(entry))
            }

        }

        return result

    }

    /**
     * Removes an item from the set. Removal is O(size/tableSize).
     */
    fun remove(value: T): Boolean {

        val index = getTableIndex(value)

        if (_table[index].remove(value)) {
            _size.decrement()
            return true
        }

        return false

    }

    /**
     * Computes a table index from the hash code of given value.
     */
    private fun getTableIndex(value: T) =
        (value.hashCode() % _tableSize + _tableSize) % _tableSize

}

//---------------------------------------------------------------------------------------------------------------------

