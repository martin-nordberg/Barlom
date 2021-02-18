//
// (C) Copyright 2018-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning.impl

import barlom.dxl.scanning.DxlToken
import barlom.dxl.scanning.EDxlTokenType.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

//---------------------------------------------------------------------------------------------------------------------

@Suppress("RemoveRedundantBackticks")
internal class DxlScannerSimpleTests
    : DxlScannerTests() {

    @Test
    fun `Token types have names`() {

        assertEquals("'['", LEFT_BRACKET.text)

    }

    @Test
    fun `Single character tokens are scanned`() {

        checkScan(
            " { } ( ) [ ] . : , = ; - ~ % @ \\ / ^ & * ! .. ",
            DxlToken(LEFT_BRACE, "{", 1, 2),
            DxlToken(RIGHT_BRACE, "}", 1, 4),
            DxlToken(LEFT_PARENTHESIS, "(", 1, 6),
            DxlToken(RIGHT_PARENTHESIS, ")", 1, 8),
            DxlToken(LEFT_BRACKET, "[", 1, 10),
            DxlToken(RIGHT_BRACKET, "]", 1, 12),
            DxlToken(DOT, ".", 1, 14),
            DxlToken(COLON, ":", 1, 16),
            DxlToken(COMMA, ",", 1, 18),
            DxlToken(EQUALS, "=", 1, 20),
            DxlToken(SEMICOLON, ";", 1, 22),
            DxlToken(SYMBOL_NAME, "-", 1, 24),
            DxlToken(TILDE, "~", 1, 26),
            DxlToken(PERCENT, "%", 1, 28),
            DxlToken(AT, "@", 1, 30),
            DxlToken(BACKSLASH, "\\", 1, 32),
            DxlToken(SYMBOL_NAME, "/", 1, 34),
            DxlToken(CARET, "^", 1, 36),
            DxlToken(AMPERSAND, "&", 1, 38),
            DxlToken(SYMBOL_NAME, "*", 1, 40),
            DxlToken(EXCLAMATION, "!", 1, 42),
            DxlToken(DOUBLE_DOT, "..", 1, 44)
        )

    }

    @Test
    fun `Placeholder names are scanned`() {

        checkScan(
            PLACEHOLDER_NAME,
            "_",
            "_abc_",
            "_Ab_234_",
            "_1_",
            "_1st_",
            "_has-been_",
            "_'pod'_"
        )

        checkScan(
            INVALID_PLACEHOLDER_NAME,
            "_abc",
            "_Ab_234",
            "_1",
            "_1st",
            "_has-been"
        )

    }

    @Test
    fun `Symbol names are scanned`() {

        checkScan(
            SYMBOL_NAME,
            "a",
            "a'",
            "abc",
            "Ab234",
            "1st",
            "1_time",
            "do_something",
            "<",
            "<>",
            "<seven>",
            "isn't",
            "<7>",
            "==",
            "+",
            "-",
            "/",
            "*",
            "**",
            "--",
            "++",
            "+=",
            "has-been"
        )

        checkScan(
            INVALID_SYMBOL_NAME,
            "a_",
            "abc_",
            "Ab234_",
            "1st_",
            "1_time_",
            "do_something__",
            "<_",
            "has-been_"
        )

    }

    @Test
    fun `Boolean literals are scanned`() {

        checkScan(BOOLEAN_LITERAL, "true", "false")

    }

    @Test
    fun `Key words are scanned`() {

        checkScan(ABSENT, "absent")
        checkScan(ALIAS, "alias")
        checkScan(AND, "and")
        checkScan(AS, "as")
        checkScan(NO_LONGER, "no-longer")
        checkScan(VALID_AS_OF, "valid-as-of")
        checkScan(WITH, "with")

    }

    @Test
    fun `Concept keywords are scanned`() {

        checkScan(
            "abc pqrs \n\n xyz_123 \n",
            DxlToken(SYMBOL_NAME, "abc", 1, 1),
            DxlToken(SYMBOL_NAME, "pqrs", 1, 5),
            DxlToken(SYMBOL_NAME, "xyz_123", 3, 2)
        )

    }

    @Test
    fun `Connector keywords are scanned`() {

        checkScan(
            "~abc ~pqrs \n\n ~xyz_123 \n",
            DxlToken(TILDE, "~", 1, 1),
            DxlToken(SYMBOL_NAME, "abc", 1, 2),
            DxlToken(TILDE, "~", 1, 6),
            DxlToken(SYMBOL_NAME, "pqrs", 1, 7),
            DxlToken(TILDE, "~", 3, 2),
            DxlToken(SYMBOL_NAME, "xyz_123", 3, 3)
        )

    }

    @Test
    fun `String literals are scanned`() {

        checkScan(
            " \"abc\" \n \"qrs\"",
            DxlToken(STRING_LITERAL, "\"abc\"", 1, 2),
            DxlToken(STRING_LITERAL, "\"qrs\"", 2, 2)
        )

    }

    @Test
    fun `Character literals are scanned`() {

        checkScan(
            " 'a' '\\n' '\\t' 'Q' ",
            DxlToken(CHARACTER_LITERAL, "'a'", 1, 2),
            DxlToken(CHARACTER_LITERAL, "'\\n'", 1, 6),
            DxlToken(CHARACTER_LITERAL, "'\\t'", 1, 11),
            DxlToken(CHARACTER_LITERAL, "'Q'", 1, 16)
        )

    }

    @Test
    fun `Integer literals are scanned`() {

        checkScan(INTEGER_LITERAL, "123", "123_456", "123'i32", "456'I64", "-123", "123'u8", "1234'U16")
        // TODO: bin, hex, oct

    }

    @Test
    fun `Floating point literals are scanned`() {

        checkScan(
            FLOATING_POINT_LITERAL,
            "123.0",
            "123F32",
            "123'f64",
            "123_456e78'f",
            "1.00E-30F64",
            "456_654d",
            "456_654.0'D"
        )

    }

    @Test
    fun `UUID literals are scanned`() {

        checkScan(
            "12345678-ABCD-EFab-cdef-901234567890\n11111111-2222-3333-4444-555555555555",
            DxlToken(UUID_LITERAL, "12345678-ABCD-EFab-cdef-901234567890", 1, 1),
            DxlToken(UUID_LITERAL, "11111111-2222-3333-4444-555555555555", 2, 1)
        )

    }

    @Test
    fun `Documentation blocks are scanned`() {

        checkScan(
            "# this is a block of documentation\n\n # this is ** another\n\n # This one \n# crosses lines\n",
            DxlToken(DOCUMENTATION, "# this is a block of documentation\n", 1, 1),
            DxlToken(DOCUMENTATION, "# this is ** another\n", 3, 2),
            DxlToken(DOCUMENTATION, "# This one \n# crosses lines\n", 5, 2)
        )

    }

}

//---------------------------------------------------------------------------------------------------------------------

