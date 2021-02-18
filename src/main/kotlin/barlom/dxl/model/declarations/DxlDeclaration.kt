//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.declarations

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlOrigin
import barlom.dxl.model.documentation.DxlOptDocumentation

//---------------------------------------------------------------------------------------------------------------------

/**
 * An abstract top level declaration.
 */
abstract class DxlDeclaration(
    origin: DxlOrigin,
    val documentation: DxlOptDocumentation
) : DxlItem(origin)

//---------------------------------------------------------------------------------------------------------------------

