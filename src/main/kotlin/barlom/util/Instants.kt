//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util

import java.time.Instant

//---------------------------------------------------------------------------------------------------------------------

object Instants {

    fun earlierOf(instant1: Instant, instant2: Instant): Instant {
        return if (instant1 <= instant2) {
            instant1
        } else {
            instant2
        }
    }

    fun laterOf(instant1: Instant, instant2: Instant): Instant {
        return if (instant1 >= instant2) {
            instant1
        } else {
            instant2
        }
    }

}

//---------------------------------------------------------------------------------------------------------------------
