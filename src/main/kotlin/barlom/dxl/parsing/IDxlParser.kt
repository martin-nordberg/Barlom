//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.parsing

import barlom.dxl.model.declarations.DxlTopLevel

interface IDxlParser {

    /**
     * Parses a top level Barlom DXL code file.
     */
    fun parseTopLevel(): DxlTopLevel

}
