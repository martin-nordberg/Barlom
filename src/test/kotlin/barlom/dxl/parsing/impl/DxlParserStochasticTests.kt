//
// (C) Copyright 2018-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.parsing.impl

import barlom.dxl.codegen.CodeGenerator
import barlom.dxl.codegen.CodeStringBuilder
import barlom.dxl.model.core.DxlNullOrigin
import barlom.dxl.model.declarations.*
import barlom.dxl.model.documentation.DxlDocumentation
import barlom.dxl.model.documentation.DxlNoDocumentation
import barlom.dxl.model.documentation.DxlOptDocumentation
import barlom.dxl.model.expressions.DxlAbsent
import barlom.dxl.model.expressions.DxlExpression
import barlom.dxl.model.expressions.literals.*
import barlom.dxl.model.labels.*
import barlom.dxl.model.properties.DxlProperties
import barlom.dxl.model.properties.DxlProperty
import barlom.dxl.model.types.DxlNoTypeRef
import barlom.dxl.model.types.DxlOptTypeRef
import barlom.dxl.model.types.DxlTypeRef
import barlom.util.TimeInterval
import barlom.util.Uuid
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.test.assertEquals

//---------------------------------------------------------------------------------------------------------------------

@Suppress("RemoveRedundantBackticks")
internal class DxlParserStochasticTests {

    @Test
    fun `Random bits of code generate, parse, and regenerate`() {

        for (trial in 1..100) {
            val topLevel1 = makeTopLevel()

            val builder1 = CodeStringBuilder()
            val codeGenerator1 = CodeGenerator(builder1)
            codeGenerator1.writeTopLevel(topLevel1)
            val code1 = builder1.toString()

            try {
                val parser = DxlParser("random.dxl", code1)

                val topLevel2 = parser.parseTopLevel()

                val builder2 = CodeStringBuilder()
                val codeGenerator2 = CodeGenerator(builder2)
                codeGenerator2.writeTopLevel(topLevel2)
                val code2 = builder2.toString()

                assertEquals(code1, code2)

//                assertEquals("Uncomment to look at a sample", code1)
            } catch (e: Exception) {
                throw Exception("FAILED:\n==============\n$code1\n==============\n", e)
            }
        }

    }

    private fun makeConceptDeclaration(): DxlConceptDeclaration {

        return DxlConceptDeclaration(
            makeConceptReference(),
            makeProperties()
        )

    }

    private fun makeConceptReference(): DxlConceptReference {

        val label = makeOptLabel()

        return DxlConceptReference(
            label,
            makeOptTypeRef(label !is DxlNoLabel)
        )

    }

    private fun makeDocumentation(): DxlOptDocumentation {

        val nDocLines = getRandom(3)

        return if (nDocLines > 0) {
            var docText = ""
            for (d in 1..nDocLines) {
                docText += "# Some documentation ${Math.random()}\n"
            }
            DxlDocumentation(DxlNullOrigin, docText)
        } else {
            DxlNoDocumentation
        }

    }

    private fun makeExpression(): DxlExpression {

        val n = getRandom(10)

        if (n == 0) {
            return DxlAbsent(DxlNullOrigin)
        }

        if (n == 1) {
            return DxlIntegerLiteral(DxlNullOrigin, getRandom(1000).toString())
        }

        if (n == 2) {
            return DxlFloatingPointLiteral(DxlNullOrigin, getRandom(100).toString() + "." + getRandom(10000))
        }

        if (n == 3) {
            return DxlCharacterLiteral(DxlNullOrigin, "'" + getRandom(9) + "'")
        }

        if (n == 4) {
            return DxlBooleanLiteral(DxlNullOrigin, "true")
        }

        if (n == 5) {
            return DxlBooleanLiteral(DxlNullOrigin, "false")
        }

        // TODO: dates, times, UUIDs, URLs, etc.

        return DxlStringLiteral(DxlNullOrigin, "\"stuff$n\"")
    }


    private fun makeNameText(base: String): String {

        val n = getRandom(10)
        val sep = "_-\$x'_-\$x_-'"[n]

        return "$base$sep$n"

    }

    private fun makeOptConnection(): DxlOptConnectionDeclaration {

        val n = getRandom(8)

        if (n == 0) {
            return DxlNoConnectionDeclaration
        }

        if (n <= 2) {
            return DxlDisconnectionDeclaration(
                makeTypeRef(),
                DxlConceptReference(
                    makeLabel(),
                    DxlNoTypeRef
                ),
                makeOptValidTime()
            )
        }

        return DxlConnectionDeclaration(
            makeTypeRef(),
            makeConceptReference(),
            makeOptValidTime(),
            makeProperties()
        )
    }

    private fun makeOptValidTime(): Instant? {
        return if (getRandom(3) == 0) {
            Instant.now()
        } else {
            null
        }
    }

    private fun makeOptValidTimeInterval(): TimeInterval? {
        val start = if (getRandom(3) == 0) {
            Instant.now()
        } else {
            return null
        }
        val end = if (getRandom(3) == 0) {
            Instant.now().plus(getRandom(200).toLong(), ChronoUnit.DAYS)
        } else {
            return null
        }
        return TimeInterval.of(start, end)
    }

    private fun makeOptLabel(): DxlOptLabel {

        val nNames = getRandom(4)

        if (nNames < 2) {
            return DxlNoLabel
        }

        return makeLabel()

    }

    private fun makeLabel(): DxlOptLabel {

        val nNames = getRandom(6)

        if (nNames < 2) {
            return DxlUuidLabel(DxlNullOrigin, Uuid.make())
        }

        if (nNames < 4) {
            return DxlSimpleName(DxlNullOrigin, makeNameText("name"))
        }

        val names: MutableList<DxlSimpleName> = mutableListOf()
        for (i in 3..nNames) {
            names.add(DxlSimpleName(DxlNullOrigin, makeNameText("name")))
        }

        return DxlQualifiedName(names)

    }

    private fun makeOptTypeRef(isForNamedElement: Boolean): DxlOptTypeRef {

        val nNames = getRandom(3)

        if (nNames == 0 && isForNamedElement) {
            return DxlNoTypeRef
        }

        if (nNames <= 1) {
            return DxlTypeRef(DxlSimpleName(DxlNullOrigin, makeNameText("Type")), isForNamedElement)
        }

        val names: MutableList<DxlSimpleName> = mutableListOf()
        for (i in 1..nNames) {
            names.add(DxlSimpleName(DxlNullOrigin, makeNameText("Type")))
        }

        return DxlTypeRef(DxlQualifiedName(names), isForNamedElement)

    }

    private fun makeProperties(): DxlProperties {

        val properties: MutableList<DxlProperty> = mutableListOf()
        val nDeclarations = getRandom(10) - 5

        for (i in 0..nDeclarations) {
            properties.add(
                DxlProperty(
                    DxlSimpleName(DxlNullOrigin, makeNameText("prop")),
                    makeExpression(),
                    makeOptValidTimeInterval()
                )
            )
        }

        return DxlProperties(properties)

    }

    private fun makeTopLevel(): DxlTopLevel {

        val aliases = DxlAliases(listOf())

        val nDeclarations = getRandom(20)

        val declList: MutableList<DxlDeclaration> = mutableListOf()
        for (i in 0..nDeclarations) {
            val decl = DxlConnectivityDeclaration(
                makeDocumentation(),
                makeConceptDeclaration(),
                makeOptConnection()
            )
            declList.add(decl)
        }

        val declarations = DxlDeclarations(declList)

        return DxlTopLevel(aliases, declarations)

    }

    private fun makeTypeRef(): DxlTypeRef {

        val nNames = getRandom(3)

        if (nNames <= 1) {
            return DxlTypeRef(DxlSimpleName(DxlNullOrigin, makeNameText("Type")), false)
        }

        val names: MutableList<DxlSimpleName> = mutableListOf()
        for (i in 1..nNames) {
            names.add(DxlSimpleName(DxlNullOrigin, makeNameText("Type")))
        }

        return DxlTypeRef(DxlQualifiedName(names), false)

    }

    private fun getRandom(upperBound: Int): Int {
        return (Math.random() * upperBound).roundToInt()
    }

}

//---------------------------------------------------------------------------------------------------------------------
