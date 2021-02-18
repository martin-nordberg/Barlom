//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.expressions.literals

import barlom.dxl.model.core.DxlOrigin
import barlom.dxl.model.expressions.DxlExpression

//---------------------------------------------------------------------------------------------------------------------

abstract class DxlLiteralExpression(
    origin: DxlOrigin
) : DxlExpression(origin) {

    abstract val text: String

}

//---------------------------------------------------------------------------------------------------------------------

