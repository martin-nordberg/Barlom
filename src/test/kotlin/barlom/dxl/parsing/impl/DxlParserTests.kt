//
// (C) Copyright 2018-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.parsing.impl

import barlom.dxl.codegen.CodeGenerator
import barlom.dxl.codegen.CodeStringBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

//---------------------------------------------------------------------------------------------------------------------

@Suppress("RemoveRedundantBackticks")
internal class DxlParserTests {

    private fun parseGenerateAndCompare(fileName: String) {

        val code = DxlParserTests::class.java.getResource(fileName).readText().trimEnd('\n') + '\n'

        val parser = DxlParser(fileName, code)

        val topLevel = parser.parseTopLevel()

        val builder = CodeStringBuilder()
        val codeGenerator = CodeGenerator(builder)
        codeGenerator.writeTopLevel(topLevel)
        val code2 = builder.toString()

        assertEquals(code, code2)

    }

    @Test
    fun `Test files parse and generate`() {
        parseGenerateAndCompare("test1.dxl")
        parseGenerateAndCompare("test2.dxl")
        parseGenerateAndCompare("test3.dxl")
    }

}

//---------------------------------------------------------------------------------------------------------------------

