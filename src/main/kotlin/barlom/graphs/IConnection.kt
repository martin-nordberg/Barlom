//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs

import barlom.util.Uuid

//---------------------------------------------------------------------------------------------------------------------

interface IConnection
    : IPropertyContainer {

    val from: IConcept

    val id: Uuid

    val to: IConcept

    val type: IConcept

}

//---------------------------------------------------------------------------------------------------------------------
