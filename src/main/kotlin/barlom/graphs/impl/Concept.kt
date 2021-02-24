//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.impl

import barlom.graphs.EMetaLevel
import barlom.graphs.IConcept
import barlom.graphs.IConnection
import barlom.util.Uuid

//---------------------------------------------------------------------------------------------------------------------

class Concept(
    override val id: Uuid,
    override val metaLevel: EMetaLevel
) : PropertyContainer(), IConcept {

    private val _connectionsIn: MutableSet<Connection> = mutableSetOf()

    private val _connectionsOut: MutableSet<Connection> = mutableSetOf()

    private val _metaConnectionsIn: MutableSet<Connection> = mutableSetOf()

    private val _metaConnectionsOut: MutableSet<Connection> = mutableSetOf()

    ////

    override val connectionsIn: Collection<IConnection>
        get() = _connectionsIn

    override val connectionsOut: Collection<IConnection>
        get() = _connectionsOut

    override val metaConnectionsIn: Collection<IConnection>
        get() = _metaConnectionsIn

    override val metaConnectionsOut: Collection<IConnection>
        get() = _metaConnectionsOut

    fun addConnectionIn(connection: Connection) {
        _connectionsIn.add(connection)
    }

    fun addConnectionOut(connection: Connection) {
        _connectionsOut.add(connection)
    }

    fun addMetaConnectionIn(connection: Connection) {
        _metaConnectionsIn.add(connection)
    }

    fun addMetaConnectionOut(connection: Connection) {
        _metaConnectionsOut.add(connection)
    }

}

//---------------------------------------------------------------------------------------------------------------------
