//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.impl

import barlom.graphs.IConnection
import barlom.util.Uuid

//---------------------------------------------------------------------------------------------------------------------

class Connection(
    id: Uuid? = null,
    override val from: Concept,
    override val type: Concept,
    override val to: Concept
) : PropertyContainer(), IConnection {

    init {
        from.addConnectionOut(this)
        to.addConnectionIn(this)
    }

    override val id = id ?: Uuid.make()

}

//---------------------------------------------------------------------------------------------------------------------
