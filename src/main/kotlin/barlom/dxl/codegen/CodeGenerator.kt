//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.codegen

import barlom.dxl.model.declarations.*
import barlom.dxl.model.documentation.DxlDocumentation
import barlom.dxl.model.documentation.DxlNoDocumentation
import barlom.dxl.model.documentation.DxlOptDocumentation
import barlom.dxl.model.expressions.DxlAbsent
import barlom.dxl.model.expressions.DxlExpression
import barlom.dxl.model.expressions.literals.DxlLiteralExpression
import barlom.dxl.model.labels.*
import barlom.dxl.model.properties.DxlProperties
import barlom.dxl.model.properties.DxlProperty
import barlom.dxl.model.types.DxlNoTypeRef
import barlom.dxl.model.types.DxlOptTypeRef
import barlom.dxl.model.types.DxlTypeRef
import barlom.util.TimeInterval
import java.time.Instant
import java.time.format.DateTimeFormatter

class CodeGenerator(
    private val output: CodeStringBuilder
) {

    fun writeTopLevel(topLevel: DxlTopLevel) {
        writeAliases(topLevel.aliases)
        writeDeclarations(topLevel.declarations)
    }

    ////

    private fun writeAlias(alias: DxlAlias) {
        output.append("alias ")
        output.append(alias.qualifiedName.text)
        output.append(" as ")
        output.append(alias.name.name)
        output.appendNewLine()
    }

    private fun writeAliases(aliases: DxlAliases) {

        for (alias in aliases) {
            writeAlias(alias)
        }

        if (aliases.isNotEmpty()) {
            output.appendNewLine()
        }

    }

    private fun writeConceptDeclaration(concept: DxlConceptDeclaration) {

        writeConceptReference(concept.reference)
        writeProperties(concept.properties)

    }

    private fun writeConceptReference(concept: DxlConceptReference) {

        writeOptLabel(concept.label)
        writeOptTypeRef(concept.typeRef, true)

    }

    private fun writeConnectionDeclaration(connection: DxlConnectionDeclaration) {

        writeOptTypeRef(connection.typeRef, false)
        output.append(" ")
        writeConceptReference(connection.concept)
        writeValidTime(connection.validTime)
        writeProperties(connection.properties)

    }

    private fun writeConnectivityDeclaration(connectivity: DxlConnectivityDeclaration) {

        val concept = connectivity.concept
        val connection = connectivity.connection

        val useMultipleLines =
            concept.properties.isNotEmpty() ||
                    when (connection) {
                        is DxlConnectionDeclaration ->
                            connection.properties.isNotEmpty()
                        else                        ->
                            false
                    }

        writeOptDocumentation(connectivity.documentation)

        writeConceptDeclaration(concept)

        if (connection !is DxlNoConnectionDeclaration) {

            if (!useMultipleLines) {
                output.append(" ")
            }
            else if (useMultipleLines && concept.properties.isEmpty()) {
                output.appendNewLine()
            }

            writeOptConnectionDeclaration(connection, useMultipleLines)

        }

        output.append(";")

    }

    private fun writeDeclaration(declaration: DxlDeclaration) {

        when (declaration) {
            is DxlConnectivityDeclaration -> writeConnectivityDeclaration(declaration)
        }

    }

    private fun writeDeclarations(declarations: DxlDeclarations) {

        for (declaration in declarations) {
            writeDeclaration(declaration)
            output.appendNewLine()
            output.appendNewLine()
        }

    }

    private fun writeDisconnectionDeclaration(disconnection: DxlDisconnectionDeclaration) {

        output.append("no-longer ")
        writeOptTypeRef(disconnection.typeRef, false)

        output.append(" ")
        writeOptLabel(disconnection.concept.label)
        writeOptTypeRef(disconnection.concept.typeRef, true)

        writeValidTime(disconnection.validTime)

    }

    private fun writeExpression(value: DxlExpression) {

        when (value) {

            is DxlLiteralExpression -> {
                output.append(value.text)
            }

            is DxlAbsent            -> {
                output.append("absent")
            }

        }

    }

    private fun writeOptConnectionDeclaration(connection: DxlOptConnectionDeclaration, useMultipleLines: Boolean) {

        var needsNewLine = useMultipleLines

        when (connection) {

            is DxlDisconnectionDeclaration -> {
                writeDisconnectionDeclaration(connection)
            }

            DxlNoConnectionDeclaration     -> {
            }

            is DxlConnectionDeclaration    -> {
                writeConnectionDeclaration(connection)
                needsNewLine = useMultipleLines && connection.properties.isEmpty()
            }

        }

        if (needsNewLine) {
            output.appendNewLine()
        }

    }

    private fun writeOptDocumentation(doc: DxlOptDocumentation) {

        when (doc) {

            is DxlDocumentation   -> {
                output.append(doc.text)
            }

            is DxlNoDocumentation -> {
            }

        }

    }

    private fun writeOptLabel(name: DxlOptLabel) {

        when (name) {

            is DxlStringLabel -> {
                output.append("\"")
                output.append(name.text)
                output.append("\"")
            }

            is DxlUuidLabel   -> {
                output.append(name.uuid.toString())
            }

            is DxlName        -> {
                output.append(name.text)
            }

            DxlNoLabel        -> {
            }

        }

    }

    private fun writeOptTypeRef(typeRef: DxlOptTypeRef, needsColon: Boolean) {

        when (typeRef) {

            is DxlTypeRef   -> {

                if (needsColon) {
                    output.append(":")
                    if (typeRef.isForNamedElement) {
                        output.append(" ")
                    }
                }

                output.append(typeRef.typeName.text)

            }

            is DxlNoTypeRef -> {
            }

        }

    }

    private fun writeProperties(properties: DxlProperties) {

        output.indented {

            var delimiter = " with"
            for (property in properties) {
                output.append(delimiter)
                output.appendNewLine()
                writeProperty(property)

                delimiter = ","
            }

            if (properties.isNotEmpty()) {
                output.appendNewLine()
            }

        }

    }

    private fun writeProperty(property: DxlProperty) {

        output.append(property.name.text)
        output.append(" = ")
        writeExpression(property.value)
        writeValidTimeInterval(property.validTimeInterval)
        writeTransactionTime(property.transactionTime)

    }

    private fun writeTransactionTime(transactionTime: Instant?) {

        if (transactionTime != null) {
            output.append(" transacted-at |")
            output.append(DateTimeFormatter.ISO_INSTANT.format(transactionTime))
            output.append("|")
        }

    }

    private fun writeValidTime(validTime: Instant?) {

        if (validTime != null) {
            output.append(" valid-as-of |")
            output.append(DateTimeFormatter.ISO_INSTANT.format(validTime))
            output.append("|")
        }

    }

    private fun writeValidTimeInterval(validTimeInterval: TimeInterval?) {

        if (validTimeInterval != null) {

            if (validTimeInterval.endsInDistantFuture()) {
                output.append(" valid-as-of |")
                output.append(DateTimeFormatter.ISO_INSTANT.format(validTimeInterval.start))
                output.append("|")
            }
            else {
                output.append(" valid-during |")
                output.append(DateTimeFormatter.ISO_INSTANT.format(validTimeInterval.start))
                output.append("|..|")
                output.append(DateTimeFormatter.ISO_INSTANT.format(validTimeInterval.end))
                output.append("|")
            }

        }

    }

}
