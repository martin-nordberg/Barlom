//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning

//---------------------------------------------------------------------------------------------------------------------

/**
 * Scanner for Barlom code. Produces the individual tokens from a string of raw Barlom code.
 */
internal interface IDxlScanner {

    /**
     * Reads the next token from the input.
     */
    fun scan(): DxlToken

}

//---------------------------------------------------------------------------------------------------------------------

