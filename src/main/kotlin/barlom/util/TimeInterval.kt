//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util

import barlom.util.Instants.earlierOf
import barlom.util.Instants.laterOf
import java.time.Duration
import java.time.Instant

//---------------------------------------------------------------------------------------------------------------------

/**
 * Class representing a time interval.
 *
 * https://www.ics.uci.edu/~alspaugh/cls/shr/allen.html
 */
class TimeInterval(
    val start: Instant,
    val end: Instant
) {

    init {
        require(start < end)
    }

    // Allen Operators

    /**
     * Returns true if the end of this time interval is before the start of the [other] time interval.
     */
    fun precedes(other: TimeInterval): Boolean {
        return end < other.start
    }

    /**
     * Returns true if this interval ends where the [other] starts.
     */
    fun meets(other: TimeInterval): Boolean {
        return end == other.start
    }

    /**
     * Returns true if this time interval starts before the [other] time interval and ends within it.
     */
    fun overlaps(other: TimeInterval): Boolean {
        return start < other.start && end > other.start && end < other.end
    }

    /**
     * Returns true if this time interval ends at the same time as the [other] one and starts earlier.
     */
    fun finishedBy(other: TimeInterval): Boolean {
        return start < other.start && end == other.end
    }

    /**
     * Returns true if this time interval starts before and ends after the [other] time interval.
     */
    fun contains(other: TimeInterval): Boolean {
        return start < other.start && end > other.end
    }

    /**
     * Returns true if this time interval starts at the same time as the [other] one and ends earlier.
     */
    fun starts(other: TimeInterval): Boolean {
        return start == other.start && end < other.end
    }

    /**
     * Returns true if this time interval starts and stops at the same times as the [other].
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        return other is TimeInterval && start == other.start && end == other.end
    }

    /**
     * Returns true if this time interval starts at the same time as the [other] one and ends later.
     */
    fun startedBy(other: TimeInterval): Boolean {
        return start == other.start && end > other.end
    }

    /**
     * Returns true if this time interval starts after the [other] time interval starts and ends before it ends.
     */
    fun during(other: TimeInterval): Boolean {
        return start > other.start && end < other.end
    }

    /**
     * Returns true if this time interval ends at the same time as the [other] one and starts later.
     */
    fun finishes(other: TimeInterval): Boolean {
        return start > other.start && end == other.end
    }

    /**
     * Returns true if this time interval starts before the [other] time interval and ends within it.
     */
    fun overlappedBy(other: TimeInterval): Boolean {
        return start > other.start && start < other.end && end > other.end
    }

    /**
     * Returns true if this interval starts where the [other] ends.
     */
    fun metBy(other: TimeInterval): Boolean {
        return start == other.end
    }

    /**
     * Returns true if the start of this time interval is after the end of the [other] time interval.
     */
    fun precededBy(other: TimeInterval): Boolean {
        return start > other.end
    }

    // Composite Operators

    /**
     * Returns true if this time interval ends after the end of the [other] time interval.
     */
    fun endsAfter(other: TimeInterval): Boolean {
        return end > other.end
    }

    /**
     * Returns true if this time interval ends at or before the start of the [other] interval.
     */
    fun precedesOrMeets(other: TimeInterval): Boolean {
        return end <= other.start
    }

    /**
     * Returns true if this time interval starts before the start of the [other] time interval.
     */
    fun startsBefore(other: TimeInterval): Boolean {
        return start < other.start
    }

    /**
     * Returns true if this time interval starts at the same time as the [other] one and ends no later.
     */
    fun startsOrEquals(other: TimeInterval): Boolean {
        return start == other.start && end <= other.end
    }

    /**
     * Return true if this time interval starts no earlier than the [other] and ends no later.
     * I.e. starts, equals, during, or finishes.
     */
    fun within(other: TimeInterval): Boolean {
        return other.start <= start && end <= other.end
    }


    // Instant Comparisons

    /**
     * Returns true if the [instant] falls within the time interval.
     */
    fun contains(instant: Instant): Boolean {
        return start <= instant && (instant < end || endsInDistantFuture() && instant == Instant.MAX)
    }

    /**
     * Returns true if this time interval ends after the given [instant].
     */
    fun endsAfter(instant: Instant): Boolean {
        return end > instant
    }

    /**
     * Returns true if this time interval ends at the same time as the given [instant].
     */
    fun endsAt(instant: Instant): Boolean {
        return end == instant
    }

    /**
     * Returns true if this time interval ends before the given [instant].
     */
    fun precedes(instant: Instant): Boolean {
        return end < instant
    }

    /**
     * Returns true if this time interval starts after the given [instant].
     */
    fun precededBy(instant: Instant): Boolean {
        return start > instant
    }

    /**
     * Returns true if this time interval starts at the same time as the given [instant].
     */
    fun startsAt(instant: Instant): Boolean {
        return start == instant
    }


    // Unary Queries

    /**
     * Returns true for a time interval with unspecified start time.
     */
    fun startsInDistantPast(): Boolean {
        return start == Instant.MIN
    }

    /**
     * Returns true for a time interval with unspecified end time.
     */
    fun endsInDistantFuture(): Boolean {
        return end == Instant.MAX
    }


    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }


    /**
     * Returns the intersection of this time interval with an overlapping [other] time interval.
     */
    fun intersect(other: TimeInterval): TimeInterval {
        if (!overlaps(other) && !other.overlaps(this)) {
            throw IllegalArgumentException("Intersection of time intervals requires overlapping intervals.")
        }
        return of(laterOf(start, other.start), earlierOf(end, other.end))
    }

    /**
     * Returns the combination of this time interval with an [other] time interval that abuts or overlaps it.
     */
    fun union(other: TimeInterval): TimeInterval {
        if (end < other.start || start > other.end) {
            throw IllegalArgumentException("Union of time intervals requires contiguous intervals.")
        }
        return of(earlierOf(start, other.start), laterOf(end, other.end))
    }

    override fun toString(): String {
        return "TimeInterval(start=$start, end=$end)"
    }

    companion object {

        /**
         * The interval from earliest defined tim to latest defined time.
         */
        val ALL_TIME = TimeInterval(Instant.MIN, Instant.MAX)

        /**
         * Constructs an interval from earliest past time to given [end] time.
         */
        fun endingAt(end: Instant): TimeInterval {
            return TimeInterval(Instant.MIN, end)
        }

        /**
         * Constructs a time interval from the current time until the distant future.
         */
        fun fromNowOn(): TimeInterval {
            return TimeInterval(Instant.now(), Instant.MAX)
        }

        /**
         * Constructs a time interval with given [start] time and [duration].
         */
        fun of(start: Instant, duration: Duration): TimeInterval {
            return TimeInterval(start, start.plus(duration))
        }

        /**
         * Constructs a time interval with specified [start] time and [end] time.
         */
        fun of(start: Instant, end: Instant): TimeInterval {
            return TimeInterval(start, end)
        }

        /**
         * Constructs a time interval from given [start] time until distant future time.
         */
        fun startingAt(start: Instant): TimeInterval {
            return TimeInterval(start, Instant.MAX)
        }

        /**
         * Constructs a time interval from distant past time until the current time.
         */
        fun upUntilNow(): TimeInterval {
            return TimeInterval(Instant.MIN, Instant.now())
        }

    }

}

//---------------------------------------------------------------------------------------------------------------------
