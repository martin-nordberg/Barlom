//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.declarations

import barlom.dxl.model.core.DxlItem

//---------------------------------------------------------------------------------------------------------------------

class DxlTopLevel(
    val aliases: DxlAliases,
    val declarations: DxlDeclarations
) : DxlItem(declarations.origin)

//---------------------------------------------------------------------------------------------------------------------

