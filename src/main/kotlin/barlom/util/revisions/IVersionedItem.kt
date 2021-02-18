//
// (C) Copyright 2014-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

//---------------------------------------------------------------------------------------------------------------------

/**
 * Interface to a versioned value that supports clean up of obsolete or aborted versions.
 */
internal interface IVersionedItem {

    /**
     * The revision history encompassing this versioned item.
     */
    val revisionHistory: RevisionHistory

    /**
     * Ensures that this item has been written by no transaction other than the currently running one.
     *
     * @throws WriteConflictException if there has been another transaction writing this item.
     */
    fun ensureNotWrittenByOtherTransaction()

    /**
     * Removes an aborted revision from this versioned item.
     */
    fun removeAbortedVersion()

    /**
     * Removes any revisions older than the given one
     *
     * @param oldestUsableRevisionNumber the oldest revision number that can still be of any use.
     */
    fun removeUnusedVersions(oldestUsableRevisionNumber: Long)

}

//---------------------------------------------------------------------------------------------------------------------
