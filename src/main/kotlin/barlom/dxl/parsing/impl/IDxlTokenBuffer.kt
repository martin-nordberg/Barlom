//
// (C) Copyright 2019 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.parsing.impl

import barlom.dxl.scanning.DxlToken
import barlom.dxl.scanning.EDxlTokenType

//---------------------------------------------------------------------------------------------------------------------

/**
 * Interface defining utilities for a parser to consume tokens or look ahead in a token buffer.
 */
internal interface IDxlTokenBuffer {

    /**
     * Consumes the next [count] tokens from the buffer.
     */
    fun consume(count: Int = 1)

    /**
     * Consumes a sequence of token types if they have the given [tokenTypes].
     * @return true if tokens of the given types were consumed.
     */
    fun consumeWhen(vararg tokenTypes: EDxlTokenType): Boolean

    /**
     * Tests whether there is still input to be consumed.
     * @return true if there are more tokens to consume.
     */
    fun hasLookAhead(): Boolean

    /**
     * Tests whether the next token to be consumed has the given [tokenType].
     * @return true if the next token has the given [tokenType].
     */
    fun hasLookAhead(tokenType: EDxlTokenType): Boolean

    /**
     * Tests whether the token [count] steps ahead in the input has given [tokenType].
     * @return true if the token [count] steps away from being read has given [tokenType].
     */
    fun hasLookAhead(count: Int, tokenType: EDxlTokenType): Boolean

    /**
     * Returns the token [count] steps ahead in the input without consuming it.
     * @return the token [count] steps ahead in the input.
     */
    fun lookAhead(count: Int): DxlToken?

    /**
     * Reads one token from the input.
     * @return the token read.
     */
    fun read(): DxlToken

}

//---------------------------------------------------------------------------------------------------------------------

