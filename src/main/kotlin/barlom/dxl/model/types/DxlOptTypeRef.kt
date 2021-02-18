//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.types

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlNullOrigin
import barlom.dxl.model.core.DxlOrigin
import barlom.dxl.model.labels.DxlName


//---------------------------------------------------------------------------------------------------------------------

sealed class DxlOptTypeRef(
    origin: DxlOrigin
) : DxlItem(origin)

//---------------------------------------------------------------------------------------------------------------------

object DxlNoTypeRef
    : DxlOptTypeRef(DxlNullOrigin)

//---------------------------------------------------------------------------------------------------------------------

class DxlTypeRef(
    val typeName: DxlName,
    val isForNamedElement: Boolean
) : DxlOptTypeRef(typeName.origin)

//---------------------------------------------------------------------------------------------------------------------

