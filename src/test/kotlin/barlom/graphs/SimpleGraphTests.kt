//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs

import barlom.graphs.impl.Graph
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


//---------------------------------------------------------------------------------------------------------------------

class SimpleGraphTests {

    @Test
    fun `Property graphs hold concepts`() {

        val g = Graph()

        g.establishConcept { }
        g.establishConcept { }
        g.establishConcept { }

        assertEquals(3, g.concepts.size)

    }


    @Test
    fun `Property graphs enable connections`() {

        val g = Graph()

        val t1 = g.establishConcept { }

        val c1 = g.establishConcept { }
        val c2 = g.establishConcept { }
        val c3 = g.establishConcept { }

        g.establishConnection(from = c1, to = c2, type = t1) {}
        g.establishConnection(from = c1, to = c3, type = t1) {}
        g.establishConnection(from = c2, to = c3, type = t1) {}

        assertEquals(3, g.connections.size)

    }


}

//---------------------------------------------------------------------------------------------------------------------
