//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.expressions

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlOrigin

//---------------------------------------------------------------------------------------------------------------------

// TODO: sealed class
// TODO: not sure an expression is ever optional
abstract class DxlOptExpression(
    origin: DxlOrigin
) : DxlItem(origin)

//---------------------------------------------------------------------------------------------------------------------

