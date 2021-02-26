//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.properties

import java.time.Instant

//---------------------------------------------------------------------------------------------------------------------

interface IPropertyValueHistory {

    /** Returns the property's value at a given instant in time together with the span of time it had that value. */
    fun get(
        validTime: Instant = Instant.now(),
        transactionTime: Instant = Instant.now()
    ): TimeSpanningPropertyValue

    /** Sets [newValues] for a property given at a given [transactionTime]. */
    fun set(
        transactionTime: Instant = Instant.now(),
        vararg newValues: TimeSpanningPropertyValue
    )

}

//---------------------------------------------------------------------------------------------------------------------
