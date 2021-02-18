//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs

import barlom.graphs.properties.IPropertyValueHistory
import barlom.graphs.properties.TimeSpanningPropertyValue
import java.time.Instant

//---------------------------------------------------------------------------------------------------------------------

/**
 * An entity that has properties retrievable by name.
 */
interface IPropertyContainer {

    /** Adds a property by [propertyName]. */
    fun add(
        propertyName: String,
        initialAssertionTime: Instant,
        vararg initialValues: TimeSpanningPropertyValue
    )

    /** @return a property by [propertyName]. */
    operator fun get(propertyName: String): IPropertyValueHistory?

    /** @return true if this concept has a property with given [propertyName]. */
    fun hasProperty(propertyName: String): Boolean

    /** @return the names of all properties relevant for this entity. */
    fun propertyNames(): Set<String>

}

//---------------------------------------------------------------------------------------------------------------------

