//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning

//---------------------------------------------------------------------------------------------------------------------

/**
 * Class serving to "tokenize" the characters of a string. Serves to mark the beginnings and ends of tokens
 * under the direction of a scanner. Counts lines and columns by character position. Lines and columns are 1-based.
 * Produces tokens from a marked starting point and a look-ahead ending point. Provides one character of
 * look-ahead to the scanner.
 */
internal interface IStringTokenizer {

    /**
     * Advances the position of this tokenizer. Moves the look ahead index forward one character (unless it is
     * already at the end of the input). Tracks the line number and column number. (Line breaks are denoted by '\n').
     */
    fun advance()

    /**
     * Advances the position of this tokenizer then returns the next look ahead character.
     */
    fun advanceAndLookAhead(): Char

    /**
     * Advances the position of this tokenizer then extracts a token from marked position up to (but not including)
     * the look ahead position.
     * @return the token extracted.
     */
    fun <TokenType, Token> advanceAndExtractTokenFromMark(
        tokenType: TokenType,
        makeToken: (type: TokenType, text: String, line: Int, column: Int) -> Token
    ): Token

    /**
     * Extracts a token from marked position up to (but not including) the look ahead position. Sets the text of the
     * token as that substring and the line and column of the token as the marked line and column. Gives the token
     * the given [tokenType].
     * @return the token extracted.
     */
    fun <TokenType, Token> extractTokenFromMark(
        tokenType: TokenType,
        makeToken: (type: TokenType, text: String, line: Int, column: Int) -> Token
    ): Token

    /**
     * Returns the text of the token that would be extracted by a call to extractTokenFromMark.
     * @return the text of the potential token to be extracted.
     */
    fun extractedTokenText(): String

    /**
     * @return the next not-yet-read character from the input string.
     */
    fun lookAhead(): Char

    /**
     * Sets the marked position to be the same as the look-ahead position. I.e. the next not-yet-read character
     * will become the first character of the next not-yet-scanned token.
     */
    fun mark()

    /**
     * Sets the marked position to be the same as the look-ahead position then advances the look ahead one character.
     */
    fun markAndAdvance()

    ////

    companion object {

        /**
         * Pseudo character representing the end of input, i.e. the virtual character one index past the end of
         * the input string.
         */
        const val END_OF_INPUT_CHAR: Char = 0.toChar()

    }

}

//---------------------------------------------------------------------------------------------------------------------

