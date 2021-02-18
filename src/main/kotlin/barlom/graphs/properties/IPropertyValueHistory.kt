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
        stateTime: Instant = Instant.now(),
        assertionTime: Instant = Instant.now()
    ): TimeSpanningPropertyValue

    /** Sets [newValues] for a property given at a given [assertionTime]. */
    fun set(
        assertionTime: Instant = Instant.now(),
        vararg newValues: TimeSpanningPropertyValue
    )

}

//---------------------------------------------------------------------------------------------------------------------

