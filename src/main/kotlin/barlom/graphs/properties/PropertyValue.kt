//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.properties

import barlom.util.TimeInterval

//---------------------------------------------------------------------------------------------------------------------

sealed class PropertyValue()

//---------------------------------------------------------------------------------------------------------------------

object AbsentPropertyValue
    : PropertyValue()

//---------------------------------------------------------------------------------------------------------------------

class BooleanPropertyValue(
    val value: Boolean
) : PropertyValue()

//---------------------------------------------------------------------------------------------------------------------

class DoublePropertyValue(
    val value: Double
) : PropertyValue()

//---------------------------------------------------------------------------------------------------------------------

class FloatPropertyValue(
    val value: Float
) : PropertyValue()

//---------------------------------------------------------------------------------------------------------------------

class IntPropertyValue(
    val value: Int
) : PropertyValue() {

    override fun toString(): String {
        return "IntPropertyValue(value=$value)"
    }

}

//---------------------------------------------------------------------------------------------------------------------

class StringPropertyValue(
    val value: String
) : PropertyValue()

//---------------------------------------------------------------------------------------------------------------------

data class TimeSpanningPropertyValue(
    val state: PropertyValue,
    val interval: TimeInterval
) {

    fun extendedThrough(followingInterval: TimeInterval): TimeSpanningPropertyValue {
        return TimeSpanningPropertyValue(
            this.state,
            TimeInterval.of(this.interval.start, followingInterval.end)
        )
    }

    fun spanningAfter(containedInterval: TimeInterval): TimeSpanningPropertyValue {
        return TimeSpanningPropertyValue(
            this.state,
            TimeInterval.of(containedInterval.start, this.interval.end)
        )
    }

    fun spanningUpTo(followingInterval: TimeInterval): TimeSpanningPropertyValue {
        return TimeSpanningPropertyValue(
            this.state,
            TimeInterval.of(this.interval.start, followingInterval.start)
        )
    }

    override fun toString(): String {
        return "TimeSpanningPropertyValue(state=$state, interval=$interval)"
    }


}

//---------------------------------------------------------------------------------------------------------------------
