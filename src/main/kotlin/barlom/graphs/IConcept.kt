//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs

import barlom.util.Uuid

//---------------------------------------------------------------------------------------------------------------------

interface IConcept
    : IPropertyContainer {

    val connectionsIn: Collection<IConnection>

    val connectionsOut: Collection<IConnection>

    val id: Uuid

}

//---------------------------------------------------------------------------------------------------------------------

