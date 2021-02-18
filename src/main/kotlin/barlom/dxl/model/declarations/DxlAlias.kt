//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.declarations

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlOrigin
import barlom.dxl.model.labels.DxlQualifiedName
import barlom.dxl.model.labels.DxlSimpleName

//---------------------------------------------------------------------------------------------------------------------

class DxlAlias(
    origin: DxlOrigin,
    val name: DxlSimpleName,
    val qualifiedName: DxlQualifiedName
) : DxlItem(origin)

//---------------------------------------------------------------------------------------------------------------------

