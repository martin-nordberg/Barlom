//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.declarations

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlNullOrigin

//---------------------------------------------------------------------------------------------------------------------

class DxlDeclarations(
    private val declarations: List<DxlDeclaration> = listOf()
) : DxlItem(if (declarations.isNotEmpty()) declarations[0].origin else DxlNullOrigin),
    Iterable<DxlDeclaration> by declarations {

    fun isEmpty() =
        declarations.isEmpty()

    fun isNotEmpty() =
        declarations.isNotEmpty()

}

//---------------------------------------------------------------------------------------------------------------------

