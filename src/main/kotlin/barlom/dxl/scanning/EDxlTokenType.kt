//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning

//---------------------------------------------------------------------------------------------------------------------

internal enum class EDxlTokenType(
    val text: String
) {

    /* Key words */
    ABSENT("absent"),
    ALIAS("alias"),
    AND("and"),
    AS("as"),
    NO_LONGER("no-longer"),
    TRANSACTED_AT("transacted-at"),
    VALID_AS_OF("valid-as-of"),
    VALID_DURING("valid-during"),
    WITH("with"),

    /* Punctuation */
    AMPERSAND("'&'"),
    AT("'@'"),
    BACKSLASH("'\\'"),
    CARET("'^'"),
    COLON("':'"),
    COMMA("','"),
    DOT("'.'"),
    DOUBLE_DOT("'..'"),
    EQUALS("'='"),
    EXCLAMATION("'!'"),
    LEFT_BRACE("'{'"),
    LEFT_BRACKET("'['"),
    LEFT_PARENTHESIS("'('"),
    PERCENT("'%'"),
    QUESTION_MARK("'?'"),
    RIGHT_BRACE("'}'"),
    RIGHT_BRACKET("']'"),
    RIGHT_PARENTHESIS("')'"),
    SEMICOLON("';'"),
    TILDE("'~'"),

    /* Character/String Literals */
    CHARACTER_LITERAL("[character literal]"),
    STRING_LITERAL("[string literal]"),

    /* Bounded Literals */
    DATE_LITERAL("[date literal]"),
    DATE_TIME_LITERAL("[date-time literal]"),
    TIME_LITERAL("[time literal]"),
    URL_LITERAL("[URL literal]"),
    // TODO: regex |...|ig...
    // TODO: URL |...|

    /* Unquoted literals */
    BOOLEAN_LITERAL("[boolean literal]"),
    FLOATING_POINT_LITERAL("[floating point literal]"),
    INTEGER_LITERAL("[integer literal]"),
    RATIONAL_NUMBER_LITERAL("[rational number literal]"),
    UUID_LITERAL("[UUID literal]"),

    /* Symbol names */
    PLACEHOLDER_NAME("[placeholder name]"),
    SYMBOL_NAME("[symbol name]"),

    /* Documentation */
    DOCUMENTATION("[documentation]"),

    /** Errors */
    INVALID_BOUNDED_LITERAL("[invalid bounded literal]"),
    INVALID_CHARACTER("[invalid character]"),
    INVALID_FLOATING_POINT_LITERAL("[invalid floating point literal]"),
    INVALID_PLACEHOLDER_NAME("[invalid placeholder name]"),
    INVALID_SYMBOL_NAME("[invalid symbol name]"),
    UNTERMINATED_CHARACTER_LITERAL("[unterminated character literal]"),
    UNTERMINATED_DOCUMENTATION("[unterminated documentation]"),
    UNTERMINATED_BOUNDED_LITERAL("[unterminated bounded literal]"),
    UNTERMINATED_STRING_LITERAL("[unterminated string literal]"),

    /** End of input. */
    END_OF_INPUT("[end of input]");

    ////

    override fun toString(): String {
        return text
    }

}

//---------------------------------------------------------------------------------------------------------------------
