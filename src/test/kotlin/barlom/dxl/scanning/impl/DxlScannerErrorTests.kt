//
// (C) Copyright 2018-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning.impl

import barlom.dxl.scanning.DxlToken
import barlom.dxl.scanning.EDxlTokenType.*
import org.junit.jupiter.api.Test

//---------------------------------------------------------------------------------------------------------------------

@Suppress("RemoveRedundantBackticks")
internal class DxlScannerErrorTests
    : DxlScannerTests() {

    @Test
    fun `Unterminated string literals are scanned`() {

        checkScan(
            " key \"first \n\"second ",
            DxlToken(SYMBOL_NAME, "key", 1, 2),
            DxlToken(UNTERMINATED_STRING_LITERAL, "\"first ", 1, 6),
            DxlToken(UNTERMINATED_STRING_LITERAL, "\"second ", 2, 1)
        )

    }

    @Test
    fun `Empty concept keywords are scanned`() {

        checkScan(
            "key 2\n- ok",
            DxlToken(SYMBOL_NAME, "key", 1, 1),
            DxlToken(INTEGER_LITERAL, "2", 1, 5),
            DxlToken(SYMBOL_NAME, "-", 2, 1),
            DxlToken(SYMBOL_NAME, "ok", 2, 3)
        )

    }

    @Test
    fun `Unterminated character literals are scanned`() {

        checkScan(
            " key '1\n'2",
            DxlToken(SYMBOL_NAME, "key", 1, 2),
            DxlToken(UNTERMINATED_CHARACTER_LITERAL, "'1", 1, 6),
            DxlToken(UNTERMINATED_CHARACTER_LITERAL, "'2", 2, 1)
        )

    }

    @Test
    fun `Unterminated documentation is scanned`() {

        checkScan(
            "# starts but does not end",
            DxlToken(UNTERMINATED_DOCUMENTATION, "# starts but does not end", 1, 1)
        )

    }

    @Test
    fun `Invalid bounded literals are scanned`() {

        checkScan(
            " |1234-1234-1234 |abcW |123456789--|",
            DxlToken(UNTERMINATED_BOUNDED_LITERAL, "|1234-1234-1234", 1, 2),
            DxlToken(UNTERMINATED_BOUNDED_LITERAL, "|abcW", 1, 18),
            DxlToken(INVALID_BOUNDED_LITERAL, "|123456789--|", 1, 24)
        )

    }

}

//---------------------------------------------------------------------------------------------------------------------

