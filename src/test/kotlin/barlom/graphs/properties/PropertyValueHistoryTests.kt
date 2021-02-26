//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.properties

import barlom.graphs.properties.impl.PropertyValueHistory
import barlom.util.TimeInterval
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.math.min
import kotlin.test.assertEquals


//---------------------------------------------------------------------------------------------------------------------

class PropertyValueHistoryTests {

    @Test
    fun `Property value histories accumulate simple value histories correctly`() {

        val times = listOf(
            Instant.parse("2020-01-01T12:00:00Z"),
            Instant.parse("2020-02-01T12:00:00Z"),
            Instant.parse("2020-02-02T12:01:00Z"),
            Instant.parse("2020-03-31T12:00:00Z"),
            Instant.parse("2020-04-01T12:00:00Z"),
            Instant.MAX
        )

        val values = listOf(
            IntPropertyValue(0),
            IntPropertyValue(1),
            IntPropertyValue(2),
            IntPropertyValue(3),
            IntPropertyValue(4),
            IntPropertyValue(4)
        )

        val history = PropertyValueHistory(
            times[0],
            TimeSpanningPropertyValue(values[0], TimeInterval.startingAt(times[0]))
        )

        for (transactionTime in times) {
            for (validTime in times) {
                assertEquals(values[0], history.get(validTime, transactionTime).state)
            }
        }

        for (i in 1..4) {
            history.set(
                times[i],
                TimeSpanningPropertyValue(values[i], TimeInterval.startingAt(times[i]))
            )

            for (iA in 0..5) {
                for (iS in 0..5) {
                    val expected = values[min(iA, min(iS, i))]
                    assertEquals(expected, history.get(times[iS], times[iA]).state)
                }
            }
        }

    }


}

//---------------------------------------------------------------------------------------------------------------------
