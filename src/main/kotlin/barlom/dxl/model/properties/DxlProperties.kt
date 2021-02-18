//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.properties

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlNullOrigin

//---------------------------------------------------------------------------------------------------------------------

class DxlProperties(
    private val properties: List<DxlProperty>
) : DxlItem(if (properties.isNotEmpty()) properties[0].origin else DxlNullOrigin),
    Iterable<DxlProperty> by properties {

    fun isEmpty() =
        properties.isEmpty()

    fun isNotEmpty() =
        properties.isNotEmpty()

}

//---------------------------------------------------------------------------------------------------------------------

