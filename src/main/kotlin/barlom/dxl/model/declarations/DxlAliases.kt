//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.declarations

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlNullOrigin

//---------------------------------------------------------------------------------------------------------------------

class DxlAliases(
    private val aliases: List<DxlAlias>
) : DxlItem(if (aliases.isNotEmpty()) aliases[0].origin else DxlNullOrigin),
    Iterable<DxlAlias> by aliases {

    fun isEmpty() =
        aliases.isEmpty()

    fun isNotEmpty() =
        aliases.isNotEmpty()

}

//---------------------------------------------------------------------------------------------------------------------

