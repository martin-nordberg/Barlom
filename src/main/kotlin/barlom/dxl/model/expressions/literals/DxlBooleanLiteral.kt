//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.expressions.literals

import barlom.dxl.model.core.DxlOrigin

//---------------------------------------------------------------------------------------------------------------------

class DxlBooleanLiteral(
    origin: DxlOrigin,
    override val text: String
) : DxlLiteralExpression(origin)

//---------------------------------------------------------------------------------------------------------------------

