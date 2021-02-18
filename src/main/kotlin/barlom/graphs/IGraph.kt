//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs

import barlom.util.Uuid

//---------------------------------------------------------------------------------------------------------------------

interface IGraph {

    fun establishConcept(
        id: Uuid? = null,
        type: IConcept? = null,
        initialize: (IConcept) -> Unit
    ): IConcept

    fun establishConnection(
        id: Uuid? = null,
        from: IConcept,
        to: IConcept,
        type: IConcept,
        initialize: (IConnection) -> Unit
    ): IConnection

}

//---------------------------------------------------------------------------------------------------------------------

