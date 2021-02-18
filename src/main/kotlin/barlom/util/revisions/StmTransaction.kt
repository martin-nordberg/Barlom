//
// (C) Copyright 2014-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

//---------------------------------------------------------------------------------------------------------------------

/**
 * Utility class for managing in-memory transactions. The code is similar to "versioned boxes", the concept behind JVSTM
 * for software transactional memory. However, this code is much more streamlined, though very experimental.
 */
internal class StmTransaction(

    /**
     * The revision history that created this transaction.
     */
    val revisionHistory: RevisionHistory,

    /**
     * The revision number of information to be read by this transaction.
     */
    val sourceRevisionNumber: Long,

    /**
     * The revision number of information written by this transaction (negative while transaction is running;
     * positive after committed; zero if aborted).
     */
    val targetRevisionNumber: AtomicRevisionNumber

) {

    /**
     * A description of the work of this transaction for history purposes.
     */
    private var _description: String = "In progress ..."

    /**
     * A newer revision number seen during reading will cause a write conflict if anything writes through this
     * transaction.
     */
    private var _newerRevisionSeen: Boolean = false

    /**
     * The versioned items read by this transaction.
     */
    private val _versionedItemsRead: MutableSet<IVersionedItem> = mutableSetOf()

    /**
     * The versioned item written by this transaction.
     */
    private val _versionedItemsWritten: MutableSet<IVersionedItem> = mutableSetOf()


    var description: String
        get() = _description
        set(value) {
            check(_description == "In progress ...") { "Cannot change transaction description after first set." }
            _description = value
        }

    /**
     * The versioned items read by this transaction.
     */
    val versionedItemsRead: Set<IVersionedItem>
        get() = _versionedItemsRead

    /**
     * The versioned item written by this transaction.
     */
    val versionedItemsWritten: Set<IVersionedItem>
        get() = _versionedItemsWritten


    /**
     * Aborts this transaction; abandons the revisions made by the transaction.
     */
    fun abort() {

        // Revision number = 0 indicates an aborted transaction.
        targetRevisionNumber.set(0L)

        // Clean up aborted revisions ...
        _versionedItemsWritten.forEach(IVersionedItem::removeAbortedVersion)

        _versionedItemsRead.clear()
        _versionedItemsWritten.clear()

    }

    /**
     * Tracks all versioned items read by this transaction. The transaction will confirm that all these items remain
     * unwritten by some other transaction before this transaction commits.
     *
     * @param versionedItem the item that has been read.
     */
    fun addVersionedItemRead(versionedItem: IVersionedItem) {

        // Track versioned items read by this transaction.
        _versionedItemsRead.add(versionedItem)

    }

    /**
     * Tracks all versioned items written by this transaction. The versions written by this transaction will be cleaned
     * up after the transaction aborts. Any earlier versions will be cleaned up after all transactions using any earlier
     * versions and their source have completed.
     *
     * @param versionedItem the item that has been written.
     */
    fun addVersionedItemWritten(versionedItem: IVersionedItem) {

        // Track all versioned items written by this transaction.
        _versionedItemsWritten.add(versionedItem)

        // If we have already seen a write conflict, fail early.
        if (_newerRevisionSeen) {
            throw WriteConflictException()
        }

    }

    /**
     * Commits this transaction.
     *
     * @throws WriteConflictException if some other transaction has concurrently written values read during this
     *                                transaction.
     */
    fun commit() {

        // Make the synchronized changed to make the transaction permanent.
        if (!_versionedItemsWritten.isEmpty()) {
            revisionHistory.writeTransaction(this)
        }

        // No longer hang on to the items read.
        _versionedItemsRead.clear()

    }

    /**
     * Takes note that some read operation has seen a newer version and will certainly fail with a write conflict if
     * this transaction writes anything. Fails immediately if this transaction has already written anything.
     */
    fun setNewerRevisionSeen() {

        // If we have previously written something, then we've detected a write conflict; fail early.
        if (!_versionedItemsWritten.isEmpty()) {
            throw WriteConflictException()
        }

        // Track the newer revision number to fail early if we subsequently write something.
        _newerRevisionSeen = true

    }

}

//---------------------------------------------------------------------------------------------------------------------

