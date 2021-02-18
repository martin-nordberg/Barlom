//
// (C) Copyright 2017-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions


//---------------------------------------------------------------------------------------------------------------------

/** Type for a revision number with atomic updates. */
typealias AtomicRevisionNumber = java.util.concurrent.atomic.AtomicLong

//---------------------------------------------------------------------------------------------------------------------

/** Type for a reference with atomic updates. */
typealias RevAtomicReference<T> = java.util.concurrent.atomic.AtomicReference<T>

//---------------------------------------------------------------------------------------------------------------------

/** Synonym for a thread local value. */
typealias RevThreadLocal<T> = java.lang.ThreadLocal<T>

//---------------------------------------------------------------------------------------------------------------------

/** Type for the hash code of a versioned item. */
typealias VersionedItemHashCode = java.util.concurrent.atomic.AtomicInteger

//---------------------------------------------------------------------------------------------------------------------

/** Increments the priority of the current thread. */
internal fun incrementThreadPriority() {

    if (Thread.currentThread().priority < Thread.MAX_PRIORITY) {
        Thread.currentThread().priority = Thread.currentThread().priority + 1
    }

}

//---------------------------------------------------------------------------------------------------------------------

/** Restores the thread priority to normal. */
internal fun restoreNormalThreadPriority() {
    Thread.currentThread().priority = Thread.NORM_PRIORITY
}

//---------------------------------------------------------------------------------------------------------------------
