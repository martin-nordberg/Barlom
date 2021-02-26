//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.properties.impl

import barlom.graphs.properties.AbsentPropertyValue
import barlom.graphs.properties.IPropertyValueHistory
import barlom.graphs.properties.TimeSpanningPropertyValue
import barlom.util.TimeInterval
import java.time.Instant


//---------------------------------------------------------------------------------------------------------------------

/**
 * Data structure holding the bitemporal history of a property value.
 */
class PropertyValueHistory(
    initialTransactionTime: Instant,
    vararg initialValues: TimeSpanningPropertyValue
) : IPropertyValueHistory {

    /** List of lists of property values by transaction time and valid time. */
    private val valueHistory: MutableList<Pair<Instant, List<TimeSpanningPropertyValue>>> = mutableListOf()

    init {

        // The initial list of states (to be built).
        val states: MutableList<TimeSpanningPropertyValue> = mutableListOf()

        // Start with absent from distant past if needed.
        var i = 0
        var stagedValue = if (initialValues.first().interval.startsInDistantPast()) {
            i += 1
            initialValues.first()
        }
        else {
            TimeSpanningPropertyValue(
                AbsentPropertyValue,
                TimeInterval.endingAt(initialValues.first().interval.start)
            )
        }

        // Add the known values.
        while (i < initialValues.size) {
            val nextValue = initialValues[i]

            stagedValue = if (stagedValue.state == nextValue.state) {
                stagedValue.extendedThrough(nextValue.interval)
            }
            else {
                states.add(stagedValue)
                nextValue
            }

            i += 1
        }

        states.add(stagedValue)

        // End with absent to distant future if needed.
        if (!initialValues.last().interval.endsInDistantFuture()) {
            states.add(
                TimeSpanningPropertyValue(
                    AbsentPropertyValue,
                    TimeInterval.startingAt(initialValues.last().interval.end)
                )
            )
        }

        // Add to the history.
        valueHistory.add(initialTransactionTime to states)

    }

    override fun get(validTime: Instant, transactionTime: Instant): TimeSpanningPropertyValue {

        // Handle the case of the the latest transaction containing the instant.
        var right = valueHistory.size - 1

        if (valueHistory[right].first <= transactionTime) {
            return getState(validTime, valueHistory[right].second)
        }

        // Handle the case of the earliest transaction following the instant.
        var left = 0

        if (valueHistory[left].first > transactionTime) {
            return TimeSpanningPropertyValue(
                AbsentPropertyValue,
                TimeInterval.endingAt(valueHistory[left].first)
            )
        }

        // Binary search for the containing interval.
        while (left < right - 1) {

            val mid = (left + right) / 2

            if (valueHistory[mid].first <= transactionTime) {
                left = mid
            }
            else {
                right = mid
            }

        }

        check(valueHistory[left].first <= transactionTime)
        check(transactionTime < valueHistory[left + 1].first)

        return getState(validTime, valueHistory[left].second)

    }

    override fun set(transactionTime: Instant, vararg newValues: TimeSpanningPropertyValue) {

        val latestTransactionTime = valueHistory[valueHistory.size - 1].first
        val latestList = valueHistory[valueHistory.size - 1].second

        // TODO: support augmenting the latest transaction time
        require(transactionTime >= latestTransactionTime)

        val sortedNewValues = newValues.sortedBy { p -> p.interval.start }

        // Require non-overlapping new state intervals.
        for (i in 0..sortedNewValues.size - 2) {
            check(sortedNewValues[i].interval.precedesOrMeets(sortedNewValues[i + 1].interval))
        }

        // Start with an empty list to be built by merging old and new.
        val newList: MutableList<TimeSpanningPropertyValue> = mutableListOf()

        var iOld = 0
        var iNew = 0

        // Pick the first value to be added and "stage" it for further comparisons.
        lateinit var stagedValue: TimeSpanningPropertyValue

        if (latestList[0].interval.startsBefore(newValues[0].interval)) {
            stagedValue = latestList[0]
            iOld = 1
        }
        else {
            stagedValue = newValues[0]
            iNew = 1
        }

        // While there are both old and new entries to process ...
        while (iOld < latestList.size && iNew < newValues.size) {

            val oldValue = latestList[iOld]
            val newValue = newValues[iNew]

            // Work through the entries always taking the earlier starting one or the new entry for a tie.
            if (oldValue.interval.startsBefore(newValue.interval)) {
                check(!stagedValue.interval.precedes(oldValue.interval))

                if (stagedValue.interval.meets(oldValue.interval)) {
                    stagedValue = if (stagedValue.state == oldValue.state) {
                        stagedValue.extendedThrough(oldValue.interval)
                    }
                    else {
                        newList.add(stagedValue)
                        oldValue
                    }
                }
                else if (oldValue.interval.within(stagedValue.interval)) {
                    // Discard an old entry that has been fully upstaged by a new entry.
                }
                else if (stagedValue.state == oldValue.state) {
                    stagedValue = stagedValue.extendedThrough(oldValue.interval)
                }
                else {
                    newList.add(stagedValue.spanningUpTo(oldValue.interval))
                    stagedValue = oldValue
                }

                iOld += 1
            }
            else {
                check(!stagedValue.interval.precedes(newValue.interval))

                if (stagedValue.interval.meets(newValue.interval)) {
                    stagedValue = if (stagedValue.state == newValue.state) {
                        stagedValue.extendedThrough(newValue.interval)
                    }
                    else {
                        newList.add(stagedValue)
                        newValue
                    }
                }
                else if (stagedValue.interval.endsAfter(newValue.interval)) {
                    if (stagedValue.state != newValue.state) {
                        newList.add(stagedValue.spanningUpTo(newValue.interval))
                        newList.add(newValue)
                        stagedValue = stagedValue.spanningAfter(newValue.interval)
                    }
                }
                else if (stagedValue.state == newValue.state) {
                    stagedValue = stagedValue.extendedThrough(newValue.interval)
                }
                else {
                    newList.add(stagedValue.spanningUpTo(newValue.interval))
                    stagedValue = newValue
                }

                iNew += 1
            }

        }

        // While there remain old entries to process ...
        while (iOld < latestList.size) {
            val oldValue = latestList[iOld]

            check(!stagedValue.interval.precedes(oldValue.interval))

            if (stagedValue.interval.meets(oldValue.interval)) {
                stagedValue = if (stagedValue.state == oldValue.state) {
                    stagedValue.extendedThrough(oldValue.interval)
                }
                else {
                    newList.add(stagedValue)
                    oldValue
                }
            }
            else if (oldValue.interval.within(stagedValue.interval)) {
                // Discard an old entry that has been fully upstaged by a new entry.
            }
            else if (stagedValue.state == oldValue.state) {
                stagedValue = stagedValue.extendedThrough(oldValue.interval)
            }
            else {
                newList.add(stagedValue.spanningUpTo(oldValue.interval))
                stagedValue = oldValue
            }

            iOld += 1
        }

        // While there remain new entries to process ...
        while (iNew < newValues.size) {
            val newValue = newValues[iNew]

            check(!stagedValue.interval.precedes(newValue.interval))

            if (stagedValue.interval.meets(newValue.interval)) {
                stagedValue = if (stagedValue.state == newValue.state) {
                    stagedValue.extendedThrough(newValue.interval)
                }
                else {
                    newList.add(stagedValue)
                    newValue
                }
            }
            else if (stagedValue.interval.endsAfter(newValue.interval)) {
                if (stagedValue.state != newValue.state) {
                    newList.add(stagedValue.spanningUpTo(newValue.interval))
                    newList.add(newValue)
                    stagedValue = stagedValue.spanningAfter(newValue.interval)
                }
            }
            else if (stagedValue.state == newValue.state) {
                stagedValue = stagedValue.extendedThrough(newValue.interval)
            }
            else {
                newList.add(stagedValue.spanningUpTo(newValue.interval))
                stagedValue = newValue
            }

            iNew += 1
        }

        // Add the last staged value.
        newList.add(stagedValue)

        // Add or replace the last chain in the history.
        if (transactionTime > latestTransactionTime) {
            valueHistory.add(transactionTime to newList)
        }
        else {
            valueHistory[valueHistory.size - 1] = transactionTime to newList
        }

        checkInvariant()

    }

    /**
     * Retrieves the value corresponding to given [validTime] from the [values] already determined by client code to
     * be applicable for a specific transactionTime.
     */
    private fun getState(validTime: Instant, values: List<TimeSpanningPropertyValue>): TimeSpanningPropertyValue {

        // Handle the common case of the the latest state containing the instant.
        var right = values.size - 1

        if (values[right].interval.contains(validTime)) {
            return values[right]
        }

        // Binary search for the containing interval.
        var left = 0
        while (left < right - 1) {

            val mid = (left + right) / 2

            if (values[mid].interval.start <= validTime) {
                left = mid
            }
            else {
                right = mid
            }

        }

        check(values[left].interval.contains(validTime))

        return values[left]

    }

    private fun checkInvariant() {

        var priorTransactionTime = Instant.MIN

        for (valueEntries in valueHistory) {
            val transactionTime = valueEntries.first
            check(transactionTime > priorTransactionTime)

            val valueList = valueEntries.second

            check(valueList.isNotEmpty())
            check(valueList[0].interval.startsInDistantPast())
            check(valueList[valueList.size - 1].interval.endsInDistantFuture())

            for (i in 0..valueList.size - 2) {
                check(valueList[i].interval.meets(valueList[i + 1].interval))
                check(valueList[i].state != valueList[i + 1].state)
            }

            priorTransactionTime = transactionTime
        }

    }

}

//---------------------------------------------------------------------------------------------------------------------
