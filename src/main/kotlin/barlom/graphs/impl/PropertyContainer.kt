//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.impl

import barlom.graphs.IPropertyContainer
import barlom.graphs.properties.IPropertyValueHistory
import barlom.graphs.properties.TimeSpanningPropertyValue
import barlom.graphs.properties.impl.PropertyValueHistory
import java.time.Instant

//---------------------------------------------------------------------------------------------------------------------

abstract class PropertyContainer : IPropertyContainer {

    private val properties: MutableMap<String, IPropertyValueHistory> = mutableMapOf()

    override fun addProperty(
        propertyName: String,
        initialTransactionTime: Instant,
        vararg initialValues: TimeSpanningPropertyValue
    ) {
        require(!hasProperty(propertyName))

        properties[propertyName] = PropertyValueHistory(initialTransactionTime, *initialValues)
    }

    override fun hasProperty(propertyName: String): Boolean {
        return properties.containsKey(propertyName)
    }

    override fun get(propertyName: String): IPropertyValueHistory? {
        return properties[propertyName]
    }

    override fun propertyNames(): Set<String> {
        return properties.keys
    }

}

//---------------------------------------------------------------------------------------------------------------------
