//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertTrue


//---------------------------------------------------------------------------------------------------------------------

class TimeIntervalTests {

    @Test
    fun `Time interval Allen operators work as expected`() {

        val t = listOf(
            Instant.MIN,                                         // 0
            Instant.parse("2020-01-01T12:00:00Z"),
            Instant.parse("2020-02-01T12:00:00Z"),
            Instant.parse("2020-02-02T12:01:00Z"),
            Instant.parse("2020-03-31T12:00:00Z"),
            Instant.parse("2020-04-01T12:00:00Z"),
            Instant.MAX                                          // 6
        )

        // Starts
        for (i in 0..4) {
            assertTrue(TimeInterval.of(t[i], t[i + 1]).starts(TimeInterval.of(t[i], t[i + 2])))
            assertTrue(TimeInterval.of(t[i], t[i + 2]).startedBy(TimeInterval.of(t[i], t[i + 1])))
        }

        // Finishes
        for (i in 2..6) {
            assertTrue(TimeInterval.of(t[i - 1], t[i]).finishes(TimeInterval.of(t[i - 2], t[i])))
            assertTrue(TimeInterval.of(t[i - 2], t[i]).finishedBy(TimeInterval.of(t[i - 1], t[i])))
        }

        // During
        for (i in 1..4) {
            assertTrue(TimeInterval.of(t[i], t[i + 1]).during(TimeInterval.of(t[i - 1], t[i + 2])))
            assertTrue(TimeInterval.of(t[i - 1], t[i + 2]).contains(TimeInterval.of(t[i], t[i + 1])))
        }

        // Equals
        for (i in 0..5) {
            assertTrue(TimeInterval.of(t[i], t[i + 1]).equals(TimeInterval.of(t[i], t[i + 1])))
        }

        // Overlaps
        for (i in 1..4) {
            assertTrue(TimeInterval.of(t[i - 1], t[i + 1]).overlaps(TimeInterval.of(t[i], t[i + 2])))
            assertTrue(TimeInterval.of(t[i], t[i + 2]).overlappedBy(TimeInterval.of(t[i - 1], t[i + 1])))
        }

        // Before
        for (i in 0..3) {
            assertTrue(TimeInterval.of(t[i], t[i + 1]).precedes(TimeInterval.of(t[i + 2], t[i + 3])))
            assertTrue(TimeInterval.of(t[i + 2], t[i + 3]).precededBy(TimeInterval.of(t[i], t[i + 1])))
        }

        // Meets
        for (i in 1..5) {
            assertTrue(TimeInterval.of(t[i - 1], t[i]).meets(TimeInterval.of(t[i], t[i + 1])))
            assertTrue(TimeInterval.of(t[i], t[i + 1]).metBy(TimeInterval.of(t[i - 1], t[i])))
        }

    }


}

//---------------------------------------------------------------------------------------------------------------------
