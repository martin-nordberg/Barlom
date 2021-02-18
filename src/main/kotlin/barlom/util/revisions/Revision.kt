//
// (C) Copyright 2017-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

//---------------------------------------------------------------------------------------------------------------------

class Revision internal constructor(

    /** A description of this revision for tracking purposes. */
    val description: String,

    /** The revision number. */
    val revisionNumber: Long,

    /** The versioned items written during this revision. */
    internal val versionedItemsWritten: Set<IVersionedItem>,

    /** the immediately preceding revision in a linked history of revisions. */
    priorRevisionRef: Revision?

) {

    /** The revision that occurred just before this one. */
    private val _priorRevision = RevAtomicReference<Revision?>(priorRevisionRef)


    /** Returns the revision prior to this one. */
    var priorRevision: Revision?
        get() = _priorRevision.get()
        set(value) {
            _priorRevision.set(value)
        }


    /**
     * Cleans up all the referenced versioned items written during this revision.
     */
    internal fun removeFromVersionHistory() {

        // Remove all revisions older than the one written by this transaction.
        for (versionedItem in versionedItemsWritten) {
            versionedItem.removeUnusedVersions(revisionNumber)
        }

    }

}

//---------------------------------------------------------------------------------------------------------------------
