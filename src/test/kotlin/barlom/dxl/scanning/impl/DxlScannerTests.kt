//
// (C) Copyright 2018-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning.impl

import barlom.dxl.scanning.DxlToken
import barlom.dxl.scanning.EDxlTokenType
import kotlin.test.assertEquals

//---------------------------------------------------------------------------------------------------------------------

@Suppress("RemoveRedundantBackticks")
internal abstract class DxlScannerTests {

    protected fun checkScan(code: String, vararg expectedTokens: DxlToken) {

        val scanner = DxlScanner(StringTokenizer(code))

        for (expectedToken in expectedTokens) {

            val token = scanner.scan()
            assertEquals(expectedToken.type, token.type)
            assertEquals(expectedToken.text, token.text)
            assertEquals(expectedToken.line, token.line)
            assertEquals(expectedToken.column, token.column)
            assertEquals(expectedToken.text.length, token.length)

            if (expectedToken.text.length == 1 &&
                expectedToken.type != EDxlTokenType.SYMBOL_NAME &&
                expectedToken.type != EDxlTokenType.INTEGER_LITERAL
            ) {
                assertEquals("'" + expectedToken.text + "'", expectedToken.type.toString())
            }

        }

        assertEquals(EDxlTokenType.END_OF_INPUT, scanner.scan().type)

    }

    protected fun checkScan(expectedTokenType: EDxlTokenType, vararg codes: String) {

        for (code in codes) {
            val scanner = DxlScanner(StringTokenizer(code))

            val token = scanner.scan()
            assertEquals(expectedTokenType, token.type)
            assertEquals(code, token.text)
            assertEquals(1, token.line)
            assertEquals(1, token.column)
            assertEquals(code.length, token.length)
        }

    }

}

//---------------------------------------------------------------------------------------------------------------------

