//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.parsing.impl

import barlom.dxl.scanning.DxlToken
import barlom.dxl.scanning.EDxlTokenType

//---------------------------------------------------------------------------------------------------------------------

/**
 * Interface adding the ability to read or trigger errors for expected tokens.
 */
internal interface IDxlExpectedTokenBuffer
    : IDxlTokenBuffer {

    /**
     * Throws ane error indicating the expected [description] was not found in the input.
     */
    fun expected(description: String): Nothing

    /**
     * Throws an error indicating that any one of the given [tokenTypes] was expected.
     */
    fun expected(vararg tokenTypes: EDxlTokenType): Nothing

    /**
     * Throws an error indicating that any one of the given [tokenTypes] was expected preceded by a [description]
     * of those token types.
     */
    fun expected(description: String, vararg tokenTypes: EDxlTokenType): Nothing

    /**
     * Determines whether the look ahead token occurs on a new line from the last read token.
     */
    fun hasLookAheadOnNewLine(): Boolean

    /**
     * Reads one token expected to be of the given [tokenType].
     */
    fun read(tokenType: EDxlTokenType): DxlToken

    /**
     * Reads one token expected to have one of the given [tokenTypes].
     */
    fun readOneOf(vararg tokenTypes: EDxlTokenType): DxlToken

}

//---------------------------------------------------------------------------------------------------------------------

