//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.impl

import barlom.graphs.IConcept
import barlom.graphs.IConnection
import barlom.util.Uuid

//---------------------------------------------------------------------------------------------------------------------

class Concept(id: Uuid?) : PropertyContainer(), IConcept {

    private val _connectionsIn: MutableSet<Connection> = mutableSetOf()

    private val _connectionsOut: MutableSet<Connection> = mutableSetOf()

    ////

    override val connectionsIn: Collection<IConnection>
        get() = _connectionsIn

    override val connectionsOut: Collection<IConnection>
        get() = _connectionsOut

    override val id = id ?: Uuid.make()

    fun addConnectionIn(connection: Connection) {
        _connectionsIn.add(connection)
    }

    fun addConnectionOut(connection: Connection) {
        _connectionsOut.add(connection)
    }

}

//---------------------------------------------------------------------------------------------------------------------

