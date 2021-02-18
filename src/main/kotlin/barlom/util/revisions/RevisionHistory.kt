//
// (C) Copyright 2017-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

//---------------------------------------------------------------------------------------------------------------------

class RevisionHistory(

    /** A description to use for the zeroth, do-nothing revision that starts the history. */
    firstRevisionDescription: String

) {

    /** The earliest revision that we still track. */
    private val _earliestRevision: RevAtomicReference<Revision>

    /** The last hash code used. */
    private val _lastHashCode = VersionedItemHashCode(0)

    /** Monotone decreasing revision number decremented whenever a transaction is started.
     *  (Negative revision numbers indicate transactions in progress.) */
    private val _lastPendingRevisionNumber: AtomicRevisionNumber = AtomicRevisionNumber(0L)

    /** The last revision that we have made. */
    private val _lastRevision: RevAtomicReference<Revision>

    /** The revision number in use for this history by a review operation currently running in the current thread. */
    private val _revisionNumberOfCurrentThread = RevThreadLocal<Long>()


    init {

        // The zeroth revision has no versions and no prior revision.
        val firstRevision = Revision(firstRevisionDescription, 0, setOf(), null)

        _earliestRevision = RevAtomicReference(firstRevision)
        _lastRevision = RevAtomicReference(firstRevision)

    }


    /** Returns the next available hash code for a revision. */
    internal val nextHashCode: Int
        get() = _lastHashCode.incrementAndGet()

    /** Returns the last available completed revision. */
    val lastRevision: Revision
        get() = _lastRevision.get()

    /** Returns the revision number currently being reviewed in the current thread. */
    internal val revisionNumberInCurrentThread: Long
        get() = _revisionNumberOfCurrentThread.get() ?: throw IllegalStateException(
            "Attempted to complete a review operation outside of revision history."
        )


    /**
     * Trims the revision history such that revisions before (but not including) the given one are no longer
     * available when values have been overwritten afterward.
     *
     * @param oldestUsableRevisionNumber the oldest revision number to be kept.
     */
    fun collapseRevisionsBefore(oldestUsableRevisionNumber: Long) {

        // TODO (and note synchronization difficulties...)

    }

    /**
     * Performs the work of the given read-only callback using the latest revision available.
     *
     * @param task the work to be done reading from the latest revision as of the start of the call.
     */
    fun <T> review(task: () -> T): T {
        return review(_lastRevision.get().revisionNumber, task)
    }

    /**
     * Performs the work of the given read-only callback using a specific past revision.
     *
     * @param revisionNumber the prior revision to read from during the task execution.
     * @param task the work to be done reading from the given revision.
     */
    fun <T> review(revisionNumber: Long, task: () -> T): T {

        // Check that the revision number is in range
        require(revisionNumber > 0 && revisionNumber <= _lastRevision.get().revisionNumber) {
            "Revision number out of bounds."
        }

        if (_revisionNumberOfCurrentThread.get() != null) {
            // TODO: maybe it's useful to nest with same revision number?
            throw IllegalStateException("Cannot nest review operations.")
        }

        try {
            _revisionNumberOfCurrentThread.set(revisionNumber)
            return task()
        } finally {
            _revisionNumberOfCurrentThread.set(null)
        }

    }

    /**
     * Performs the work of the given read-write callback inside a newly created transaction. The transaction
     * will read from the latest available revision as of the start of the call or a retry. It will write to the
     * next available revision.
     *
     * @param task       the work to be done inside a transaction; returns a description of that work
     * @param maxRetries the maximum number of times to retry the transaction if write conflicts are encountered (must
     *                   be zero or greater, default zero meaning try but don't retry).
     * @return a description of the transaction (as returned by the task).
     *
     * @throws MaximumRetriesExceededException if the transaction fails even after the specified number of retries.
     */
    fun update(maxRetries: Int = 0, task: () -> String): String {

        // Sanity check the input.
        require(maxRetries >= 0) { "Retry count must be greater than or equal to zero." }
        require(_transactionOfCurrentThread.get() == null) { "Update cannot be reentrant" }

        try {

            for (retry in 0..maxRetries) {

                try {

                    val transaction = StmTransaction(
                        this,
                        _lastRevision.get().revisionNumber,
                        AtomicRevisionNumber(_lastPendingRevisionNumber.decrementAndGet())
                    )

                    try {
                        _transactionOfCurrentThread.set(transaction)

                        // Execute the transactional task.
                        transaction.description = task()

                        // Commit the changes.
                        transaction.commit()

                        // If succeeded, no more retries are needed.
                        return transaction.description
                    } catch (e: Exception) {
                        // On any error abort the transaction.
                        transaction.abort()
                        throw e
                    } finally {
                        // Clear the thread's transaction.
                        _transactionOfCurrentThread.set(null)
                    }

                } catch (e: WriteConflictException) {
                    // Ignore the exception; go around the loop again....

                    // Increment the thread priority for a better chance on next try.
                    incrementThreadPriority()
                }

            }

            // If we dropped out of the loop, then we exceeded the retry count.
            throw MaximumRetriesExceededException()

        } finally {
            // Restore the thread priority after any retries.
            restoreNormalThreadPriority()
        }

    }

    /**
     * Atomically commits the given transaction.
     *
     * @param transaction the transaction to commit
     *
     * @throws WriteConflictException if some other transaction has written some value the given transaction read.
     */
    internal fun writeTransaction(transaction: StmTransaction) {

        synchronized(this) {

            // Check for conflicts.
            transaction.versionedItemsRead.forEach(IVersionedItem::ensureNotWrittenByOtherTransaction)

            val priorRevision = _lastRevision.get()
            val revisionNumber = priorRevision.revisionNumber + 1
            _lastRevision.set(
                Revision(transaction.description, revisionNumber, transaction.versionedItemsWritten, priorRevision)
            )

            // Set the revision number to a committed value.
            transaction.targetRevisionNumber.set(revisionNumber)

        }

    }

    companion object {

        /** The currently executing transaction for the current thread. */
        private val _transactionOfCurrentThread: RevThreadLocal<StmTransaction?> = RevThreadLocal()


        /**
         * Returns the revision history currently undergoing an update or review.
         */
        val currentlyInUse: RevisionHistory
            get() = transactionOfCurrentThread.revisionHistory

        /**
         * @return The transaction that has been established for the currently running thread, if any
         */
        internal val maybeTransactionOfCurrentThread: StmTransaction?
            get() {

                // Get the thread-local transaction.
                return _transactionOfCurrentThread.get()

            }

        /**
         * @return the transaction that is known to have been established for the currently running thread
         */
        internal val transactionOfCurrentThread: StmTransaction
            get() {

                // Get the thread-local transaction. If there is none, then it's a programming error.
                return _transactionOfCurrentThread.get() ?: throw IllegalStateException(
                    "Attempted to complete a transactional operation without a transaction."
                )

            }

    }

}

//---------------------------------------------------------------------------------------------------------------------
