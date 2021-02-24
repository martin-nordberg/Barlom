package barlom.graphs

import barlom.graphs.impl.Graph
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EmptyGraphTests {

    @Test
    fun `An empty graph has preloaded meta concepts`() {

        val graph = Graph()

        assertEquals(12, graph.concepts.size)
        assertEquals(28, graph.connections.size)
    }

}
