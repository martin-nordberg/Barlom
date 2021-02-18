//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning

//---------------------------------------------------------------------------------------------------------------------

internal class DxlToken(
    val type: EDxlTokenType,
    val text: String,
    val line: Int,
    val column: Int
) {

    val length: Int
        get() = text.length

}

//---------------------------------------------------------------------------------------------------------------------

