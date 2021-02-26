//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning.impl

import barlom.dxl.scanning.DxlToken
import barlom.dxl.scanning.EDxlTokenType
import barlom.dxl.scanning.EDxlTokenType.*
import barlom.dxl.scanning.IDxlScanner
import barlom.dxl.scanning.IStringTokenizer
import barlom.dxl.scanning.IStringTokenizer.Companion.END_OF_INPUT_CHAR

//---------------------------------------------------------------------------------------------------------------------

// Symbolic Tokens: letters  numbers  $  _  +  -  *  /  =  <  >

// Boolean literal     true  false
// String literal      "..."
// Character literal   '.'
// Numeric literal     [starts with digit]
// URL literal         |http://whatever.com/stuff|
// UUID literal        12345678-ABCD-EFab-cdef-901234567890   [symbol matching UUID regex]
// Date/time literal   |2017-10-20T13:45|
// Regular Expression  |/.../ig|

// Name : Type
// (  )   -- parameters / arguments
// {  }   -- contents
// [  ]   -- indexes

/**
 * Scanner for Barlom code. Orchestrates the given [input] tokenizer to produce the individual tokens of a string
 * of raw Barlom code.
 */
internal class DxlScanner(
    private val input: IStringTokenizer
) : IDxlScanner,
    IStringTokenizer by input {

    override fun scan(): DxlToken {

        var nextChar = lookAhead()

        // Ignore whitespace.
        while (isWhitespace(nextChar)) {
            nextChar = advanceAndLookAhead()
        }

        // Consume the one character after marking the start of a token.
        markAndAdvance()

        return when (nextChar) {

            // Single character punctuation tokens
            // TODO: '{-' '{<' '{--', '{:', '{.', '{#', '{!', '{%', '{*', '{+' and similar custom brackets
            '&' -> extractTokenFromMark(AMPERSAND)
            '@' -> extractTokenFromMark(AT)
            '\\' -> extractTokenFromMark(BACKSLASH)
            '^' -> extractTokenFromMark(CARET)
            ':' -> extractTokenFromMark(COLON)
            ',' -> extractTokenFromMark(COMMA)
            '!' -> extractTokenFromMark(EXCLAMATION)
            '{' -> extractTokenFromMark(LEFT_BRACE)
            '[' -> extractTokenFromMark(LEFT_BRACKET)
            '(' -> extractTokenFromMark(LEFT_PARENTHESIS)
            '?' -> extractTokenFromMark(QUESTION_MARK)
            '%' -> extractTokenFromMark(PERCENT)
            '}' -> extractTokenFromMark(RIGHT_BRACE)
            ']' -> extractTokenFromMark(RIGHT_BRACKET)
            ')' -> extractTokenFromMark(RIGHT_PARENTHESIS)
            ';' -> extractTokenFromMark(SEMICOLON)
            '~' -> extractTokenFromMark(TILDE)

            // dot or double dot
            '.' -> scanDots()

            // documentation or "/"
            '#' -> scanDocumentation()

            // String literal
            '"' -> scanStringLiteral()

            // Character literal
            '\'' -> scanCharacterLiteral()

            // Other literals delimited by vertical lines
            '|' -> scanBoundedLiteral()

            // Identifiers serving as pattern placeholders
            '_' -> scanPlaceholderName()

            // Code enclosed in back ticks
// TODO            '`'  -> scanQuotedCode()

            // End of input sentinel
            END_OF_INPUT_CHAR -> extractTokenFromMark(END_OF_INPUT)

            // Miscellaneous
            else -> when {
                // Scan a symbolic token (name or number).
                isSymbolicTokenStartCharacter(nextChar) -> scanSymbolicToken()

                // Error - nothing else it could be.
                else -> extractTokenFromMark(INVALID_CHARACTER)
            }

        }
    }

    ////

    /**
     * Advances the position of the input tokenizer then extracts a token from its marked position.
     * @return the token extracted.
     */
    private fun advanceAndExtractTokenFromMark(tokenType: EDxlTokenType): DxlToken {
        return input.advanceAndExtractTokenFromMark(tokenType, ::DxlToken)
    }

    /**
     * Extracts a token from the marked position of the input tokenizer. Gives the token the given [tokenType].
     * @return the token extracted.
     */
    private fun extractTokenFromMark(tokenType: EDxlTokenType): DxlToken {
        return input.extractTokenFromMark(tokenType, ::DxlToken)
    }

    /**
     * @return true if the given [character] can be a character in a placeholder name.
     */
    private fun isPlaceholderNameCharacter(character: Char): Boolean {
        // TODO: Unicode letters
        return "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_'-$".contains(character)
    }

    /**
     * @return true if the given [character] can be the first character of a symbolic token.
     */
    private fun isSymbolicTokenStartCharacter(character: Char): Boolean {
        // TODO: Unicode letters
        return "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$+-/*=<>".contains(character)
    }

    /**
     * @return true if the given [character] can be a subsequent character of a symbolic token.
     */
    private fun isSymbolicTokenPartCharacter(character: Char): Boolean {
        // TODO: Unicode letters
        return "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_'$+-/*=<>".contains(character)
    }

    /**
     * @return true if the given [character] is whitespace.
     */
    private fun isWhitespace(character: Char): Boolean {
        return " \t\r\n".contains(character)
    }

    /**
     * Scans a character literal token after its opening "'" character has been marked and consumed in the tokenizer.
     */
    private fun scanCharacterLiteral(): DxlToken {

        // TODO: proper treatment of escape characters

        var nextChar = lookAhead()

        while (nextChar != '\'') {

            if (nextChar == '\n' || nextChar == END_OF_INPUT_CHAR) {
                return extractTokenFromMark(UNTERMINATED_CHARACTER_LITERAL)
            }

            nextChar = advanceAndLookAhead()

        }

        return advanceAndExtractTokenFromMark(CHARACTER_LITERAL)

    }

    /**
     * Scans a block of documentation after its opening '#' character has been marked and consumed in
     * the tokenizer.
     */
    private fun scanDocumentation(): DxlToken {

        var nextChar = '#'

        // Multiple lines starting with '#' are all one documentation block.
        while (nextChar == '#') {

            nextChar = lookAhead()

            // Read to the end of the line.
            while (nextChar != '\n') {

                if (nextChar == END_OF_INPUT_CHAR) {
                    return extractTokenFromMark(UNTERMINATED_DOCUMENTATION)
                }

                nextChar = advanceAndLookAhead()

            }

            // Consume the new line character.
            nextChar = advanceAndLookAhead()

            // Ignore leading whitespace on subsequent lines.
            while (" \t".contains(nextChar)) {
                nextChar = advanceAndLookAhead()
            }

            nextChar = lookAhead()

        }

        return extractTokenFromMark(DOCUMENTATION)

    }

    /**
     * Scans a bounded literal after the opening vertical bar character has been marked and consumed.
     */
    private fun scanBoundedLiteral(): DxlToken {

        var nextChar = lookAhead()

        while (nextChar != '|') {

            if (isWhitespace(nextChar) || nextChar == '\n' || nextChar == END_OF_INPUT_CHAR) {
                return extractTokenFromMark(UNTERMINATED_BOUNDED_LITERAL)
            }

            nextChar = advanceAndLookAhead()

        }

        val token = extractedTokenText()
        val innerToken = token.substring(1)

        if (innerToken.matches(DATE_TIME_REGEX)) {
            return advanceAndExtractTokenFromMark(DATE_TIME_LITERAL)
        }

        if (innerToken.matches(DATE_REGEX)) {
            return advanceAndExtractTokenFromMark(DATE_LITERAL)
        }

        if (innerToken.matches(TIME_REGEX)) {
            return advanceAndExtractTokenFromMark(TIME_LITERAL)
        }

        if (innerToken.matches(URL_REGEX)) {
            return advanceAndExtractTokenFromMark(URL_LITERAL)
        }

        return advanceAndExtractTokenFromMark(INVALID_BOUNDED_LITERAL)

    }

    /**
     * Scans a single or double dot token.
     */
    private fun scanDots(): DxlToken {
        return if (lookAhead() == '.') {
            advanceAndExtractTokenFromMark(DOUBLE_DOT)
        } else {
            extractTokenFromMark(DOT)
        }
    }

    /**
     * Scans a placeholder name after the opening underscore character has been marked and consumed.
     */
    private fun scanPlaceholderName(): DxlToken {

        while (isPlaceholderNameCharacter(lookAhead())) {
            advance()
        }

        val token = extractedTokenText()

        if (!token.endsWith("_")) {
            return extractTokenFromMark(INVALID_PLACEHOLDER_NAME)
        }

        return extractTokenFromMark(PLACEHOLDER_NAME)

    }

    /**
     * Scans a string literal after the opening double quote character has been marked and consumed.
     */
    private fun scanStringLiteral(): DxlToken {

        // TODO: proper treatment of escape characters

        // TODO: multiline strings

        var nextChar = lookAhead()

        while (nextChar != '"') {

            if (nextChar == '\n' || nextChar == END_OF_INPUT_CHAR) {
                return extractTokenFromMark(UNTERMINATED_STRING_LITERAL)
            }

            nextChar = advanceAndLookAhead()

        }

        return advanceAndExtractTokenFromMark(STRING_LITERAL)

    }

    /**
     * Scans an identifier after its first character has been marked and consumed.
     */
    private fun scanSymbolicToken(): DxlToken {

        while (isSymbolicTokenPartCharacter(lookAhead())) {
            advance()
        }

        val token = extractedTokenText()
        when (token) {
            "absent" -> return extractTokenFromMark(ABSENT)
            "alias" -> return extractTokenFromMark(ALIAS)
            "and" -> return extractTokenFromMark(AND)
            "as" -> return extractTokenFromMark(AS)
            "no-longer" -> return extractTokenFromMark(NO_LONGER)
            "transacted-at" -> return extractTokenFromMark(TRANSACTED_AT)
            "valid-as-of" -> return extractTokenFromMark(VALID_AS_OF)
            "valid-during" -> return extractTokenFromMark(VALID_DURING)
            "with" -> return extractTokenFromMark(WITH)

            "false" -> return extractTokenFromMark(BOOLEAN_LITERAL)
            "true" -> return extractTokenFromMark(BOOLEAN_LITERAL)

            "=" -> return extractTokenFromMark(EQUALS)
        }

        if (token.matches(UUID_REGEX)) {
            return extractTokenFromMark(UUID_LITERAL)
        }

        if (token.matches(INTEGER_REGEX)) {
            if (lookAhead() == '.' && token.matches(DECIMAL_NUMBER_REGEX)) {
                advance()
                while (isSymbolicTokenPartCharacter(lookAhead())) {
                    advance()
                }
                val token2 = extractedTokenText()

                return if (token2.matches(FLOATING_POINT_REGEX)) {
                    extractTokenFromMark(FLOATING_POINT_LITERAL)
                } else {
                    extractTokenFromMark(INVALID_FLOATING_POINT_LITERAL)
                }
            } else {
                return extractTokenFromMark(INTEGER_LITERAL)
            }
        }

        if (token.matches(FLOATING_POINT_REGEX)) {
            return extractTokenFromMark(FLOATING_POINT_LITERAL)
        }

        if (token.matches(RATIONAL_REGEX)) {
            return extractTokenFromMark(RATIONAL_NUMBER_LITERAL)
        }

        if (token.endsWith("_")) {
            return extractTokenFromMark(INVALID_SYMBOL_NAME)
        }

        return extractTokenFromMark(SYMBOL_NAME)

    }

    ////

    companion object {

        /** Pattern for matching date literals. */
        private val DATE_REGEX: Regex

        /** Pattern for matching date/time literals. */
        private val DATE_TIME_REGEX: Regex

        /** Pattern for a decimal integer literal or the first portion of a floating point literal. */
        private val DECIMAL_NUMBER_REGEX: Regex

        /** Pattern for matching floating point literals. */
        private val FLOATING_POINT_REGEX: Regex

        /** Pattern for matching integer literals. */
        private val INTEGER_REGEX: Regex

        /** Pattern for matching rational number literals. */
        private val RATIONAL_REGEX: Regex

        /** Pattern for matching time literals. */
        private val TIME_REGEX: Regex

        /** Pattern for matching URL literals. */
        private val URL_REGEX = Regex("^(https?|ftp)://(-\\.)?([^\\s/?.#-]+\\.?)+(/[^\\s]*)?$")

        /** Pattern for matching UUIDs. Note: does not validate UUID type bits or other details. */
        private val UUID_REGEX: Regex

        init {
            val hexDigit = "[0-9a-fA-f]"

            UUID_REGEX = Regex("^${hexDigit}{8}-${hexDigit}{4}-${hexDigit}{4}-${hexDigit}{4}-${hexDigit}{12}$")

            val binDigit = "[0-1]"
            val decDigit = "[0-9]"
            val octDigit = "[0-7]"

            val binLiteral = "0[bB]${binDigit}(_?${binDigit})*"
            val decLiteral = "${decDigit}(_?${decDigit})*"
            val hexLiteral = "0[xX]${hexDigit}(_?${hexDigit})*"
            val octLiteral = "0[oO]${octDigit}(_?${octDigit})*"

            val intSuffix = "'?[iIuU](8|16|32|64)?"

            DECIMAL_NUMBER_REGEX = Regex("^-?${decLiteral}$")
            INTEGER_REGEX = Regex("^-?(${decLiteral}|${binLiteral}|${hexLiteral}|${octLiteral})(${intSuffix})?$")
            RATIONAL_REGEX = Regex("^-?${decLiteral}/${decLiteral}$")

            val exponent = "[eE][+-]?${decLiteral}"
            val floatSuffix = "'?([fF](32|64)?|[dD])"

            FLOATING_POINT_REGEX =
                Regex("^-?${decLiteral}((\\.${decLiteral}(${exponent})?|${exponent})(${floatSuffix})?$|${floatSuffix})")

            val date = "${decDigit}{4}-${decDigit}{2}-${decDigit}{2}"

            DATE_REGEX = Regex("^${date}$")

            val timeZone = "([zZ]|[+-]${decDigit}{2}(:${decDigit}{2})?)"
            val time = "T${decDigit}{2}:${decDigit}{2}(:${decDigit}{2}(\\.${decDigit}{0,9})?)?(${timeZone})?"

            DATE_TIME_REGEX = Regex("^${date}${time}$")
            TIME_REGEX = Regex("^${time}$")
        }

    }

}

//---------------------------------------------------------------------------------------------------------------------
