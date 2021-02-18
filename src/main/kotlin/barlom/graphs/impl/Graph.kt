//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.impl

import barlom.graphs.IConcept
import barlom.graphs.IConnection
import barlom.graphs.IGraph
import barlom.util.Uuid


//---------------------------------------------------------------------------------------------------------------------

class Graph : IGraph {

    val concepts: MutableList<IConcept> = mutableListOf()

    val connections: MutableList<IConnection> = mutableListOf()

    override fun establishConcept(id: Uuid?, type: IConcept?, initialize: (IConcept) -> Unit): IConcept {
        val concept = Concept(id)

        initialize(concept)

        // TODO: set the type

        concepts.add(concept)

        return concept
    }

    override fun establishConnection(
        id: Uuid?,
        from: IConcept,
        to: IConcept,
        type: IConcept,
        initialize: (IConnection) -> Unit
    ): IConnection {
        val connection = Connection(id, from, to)

        initialize(connection)

        // TODO: set the type

        connections.add(connection)

        return connection
    }

}

//---------------------------------------------------------------------------------------------------------------------
