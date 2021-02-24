//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs

import barlom.graphs.properties.IPropertyValueHistory
import barlom.graphs.properties.StringPropertyValue
import barlom.graphs.properties.TimeSpanningPropertyValue
import barlom.util.TimeInterval
import java.time.Instant

//---------------------------------------------------------------------------------------------------------------------

/**
 * An entity that has properties retrievable by name.
 */
interface IPropertyContainer {

    /** Adds a property by [propertyName]. */
    fun addProperty(
        propertyName: String,
        initialAssertionTime: Instant,
        vararg initialValues: TimeSpanningPropertyValue
    )

    fun addProperty(
        propertyName: String,
        initialAssertionAndStateTime: Instant,
        value: String
    ) {
        addProperty(
            propertyName,
            initialAssertionAndStateTime,
            TimeSpanningPropertyValue(StringPropertyValue(value), TimeInterval.startingAt(initialAssertionAndStateTime))
        )
    }

    /** @return a property by [propertyName]. */
    operator fun get(propertyName: String): IPropertyValueHistory?

    /** @return true if this concept has a property with given [propertyName]. */
    fun hasProperty(propertyName: String): Boolean

    /** @return the names of all properties relevant for this entity. */
    fun propertyNames(): Set<String>

}

//---------------------------------------------------------------------------------------------------------------------
