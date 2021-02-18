//
// (C) Copyright 2014-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

//---------------------------------------------------------------------------------------------------------------------

/**
 * Exception thrown when a transaction fails within its specified number of retries..
 */
class MaximumRetriesExceededException
    : RuntimeException("Maximum retries exceeded.")

//---------------------------------------------------------------------------------------------------------------------
