//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.documentation

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlNullOrigin
import barlom.dxl.model.core.DxlOrigin


//---------------------------------------------------------------------------------------------------------------------

/**
 * Base class for documentation that is optional.
 */
sealed class DxlOptDocumentation(

    /** Where the documentation originated. */
    origin: DxlOrigin

) : DxlItem(origin)

//---------------------------------------------------------------------------------------------------------------------

/**
 * Null object implementation when optional documentation is not present.
 */
object DxlNoDocumentation
    : DxlOptDocumentation(DxlNullOrigin)

//---------------------------------------------------------------------------------------------------------------------

/**
 * Documentation for a parsed item.
 */
class DxlDocumentation(

    /** Where the documentation starts in the source file. */
    origin: DxlOrigin,

    /** The text of the documentation including "/*" and "*/". */
    val text: String

) : DxlOptDocumentation(origin)

//---------------------------------------------------------------------------------------------------------------------

