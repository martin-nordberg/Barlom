//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning.impl

import barlom.dxl.scanning.IStringTokenizer

//---------------------------------------------------------------------------------------------------------------------

/**
 * Class serving to "tokenize" the characters of a string. Serves to mark the beginnings and ends of tokens
 * under the direction of a scanner. Counts lines and columns by character position. Lines and columns are 1-based.
 * Produces L-Zero tokens from a marked starting point and a look-ahead ending point. Provides one character of
 * look-ahead to the scanner.
 */
internal class StringTokenizer(
    private val input: String
) : IStringTokenizer {

    /** The column number of the next not-yet-consumed character. */
    private var lookAheadColumn = 1

    /** The index (within the whole string) of the next not-yet-consumed character. */
    private var lookAheadIndex = 0

    /** The line number of the next not-yet-consumed character. */
    private var lookAheadLine: Int = 1

    /** The column number of the marked start of a token while it is under construction by the scanner. */
    private var markedColumn = 1

    /** The index (within the whole string) of the marked start of a token while it is under construction by the scanner. */
    private var markedIndex = 0

    /** The line number of the marked start of a token while it is under construction by the scanner. */
    private var markedLine: Int = 1

    ////

    /**
     * Advances the position of this tokenizer. Moves the look ahead index forward one character (unless it is
     * already at the end of the input). Tracks the line number and column number. (Line breaks are denoted by '\n').
     */
    override fun advance() {

        if (lookAheadIndex < input.length) {

            if (input[lookAheadIndex] == '\n') {
                lookAheadColumn = 1
                lookAheadLine += 1
            } else {
                lookAheadColumn += 1
            }

            lookAheadIndex += 1

        }

    }

    /**
     * Advances the position of this tokenizer then returns the next look ahead character.
     */
    override fun advanceAndLookAhead(): Char {
        advance()
        return lookAhead()
    }

    /**
     * Advances the position of this tokenizer then extracts a token from marked position up to (but not including)
     * the look ahead position.
     * @return the token extracted.
     */
    override fun <TokenType, Token> advanceAndExtractTokenFromMark(
        tokenType: TokenType,
        makeToken: (type: TokenType, text: String, line: Int, column: Int) -> Token
    ): Token {
        advance()
        return extractTokenFromMark(tokenType, makeToken)
    }

    /**
     * Extracts a token from marked position up to (but not including) the look ahead position. Sets the text of the
     * token as that substring and the line and column of the token as the marked line and column. Gives the token
     * the given [tokenType].
     * @return the token extracted.
     */
    override fun <TokenType, Token> extractTokenFromMark(
        tokenType: TokenType,
        makeToken: (type: TokenType, text: String, line: Int, column: Int) -> Token
    ): Token {
        return makeToken(tokenType, extractedTokenText(), markedLine, markedColumn)
    }

    /**
     * Returns the text of the token that would be extracted by a call to extractTokenFromMark.
     * @return the text of the potential token to be extracted.
     */
    override fun extractedTokenText(): String {
        return input.substring(markedIndex, lookAheadIndex).intern()
    }

    /**
     * @return the next not-yet-read character from the input string.
     */
    override fun lookAhead(): Char {
        return if (lookAheadIndex < input.length) input[lookAheadIndex]
        else IStringTokenizer.END_OF_INPUT_CHAR
    }

    /**
     * Sets the marked position to be the same as the look-ahead position. I.e. the next not-yet-read character
     * will become the first character of the next not-yet-scanned token.
     */
    override fun mark() {
        markedColumn = lookAheadColumn
        markedIndex = lookAheadIndex
        markedLine = lookAheadLine
    }

    /**
     * Sets the marked position to be the same as the look-ahead position then advances the look ahead one character.
     */
    override fun markAndAdvance() {
        mark()
        advance()
    }

}

//---------------------------------------------------------------------------------------------------------------------

