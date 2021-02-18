//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.impl

import barlom.graphs.IConcept
import barlom.graphs.IConnection
import barlom.util.Uuid

//---------------------------------------------------------------------------------------------------------------------

class Connection(
    id: Uuid?,
    override val from: IConcept,
    override val to: IConcept
) : PropertyContainer(), IConnection {

    init {
        (from as Concept).addConnectionOut(this)
        (to as Concept).addConnectionIn(this)
    }

    override val id = id ?: Uuid.make()

}

//---------------------------------------------------------------------------------------------------------------------

