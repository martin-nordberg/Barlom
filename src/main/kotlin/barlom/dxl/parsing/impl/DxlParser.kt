//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.parsing.impl

import barlom.dxl.model.core.DxlFileOrigin
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
import barlom.dxl.parsing.IDxlParser
import barlom.dxl.scanning.DxlToken
import barlom.dxl.scanning.EDxlTokenType.*
import barlom.util.TimeInterval
import barlom.util.Uuid
import java.time.Instant

class DxlParser(
    private val fileName: String,
    code: String
) : IDxlParser {

    private val input = DxlExpectedTokenBuffer(fileName, code)

    ////

    /**
     * Parses the top level of a DXL file.
     *
     * topLevel
     *   : alias* declaration+
     *   ;
     */
    override fun parseTopLevel(): DxlTopLevel {

        val aliases = parseAliases()

        val declarations = parseDeclarations()

        return DxlTopLevel(aliases, declarations)

    }

    ////

    /**
     * Parses zero or more alias declarations.
     *
     * aliases
     *   : "alias" qualifiedName "as" simpleName
     *   ;
     */
    private fun parseAliases(): DxlAliases {

        val aliases = mutableListOf<DxlAlias>()

        while (input.hasLookAhead(ALIAS)) {
            val aliasToken = input.read()
            val name = parseSimpleName()
            input.read(AS)
            val qualifiedName = parseQualifiedName()

            if (qualifiedName is DxlQualifiedName) {
                aliases.add(DxlAlias(aliasToken.origin, name, qualifiedName))
            }
            else {
                input.expected("aliased name to be fully qualified")
            }
        }

        return DxlAliases(aliases)

    }

    /**
     * Parses a concept declaration.
     *
     * conceptDeclaration
     *   : conceptReference properties
     *   ;
     */
    private fun parseConceptDeclaration(): DxlConceptDeclaration {

        // conceptReference
        val reference = parseConceptReference()

        // properties?
        val properties = parsePropertiesOpt()

        return DxlConceptDeclaration(reference, properties)

    }

    /**
     * conceptReference
     *   : label? typeRef?
     *   ;
     */
    private fun parseConceptReference(): DxlConceptReference {

        // label?
        val label = parseLabelOpt()

        // typeName?
        val typeRef = if (label is DxlNoLabel) {
            parseTypeRef(false)
        }
        else {
            parseTypeRefOpt()
        }

        return DxlConceptReference(label, typeRef)

    }

    /**
     * connectionDeclaration
     *   : typeRef conceptReference validTime? properties?
     *   ;
     */
    private fun parseConnectionDeclaration(): DxlConnectionDeclaration {

        // typeRef
        val typeRef = DxlTypeRef(parseQualifiedName(), false)

        // conceptReference?
        val connectedConcept = parseConceptReference()

        // validTime?
        val validTime = parseValidTimeOpt()

        // properties?
        val properties = parsePropertiesOpt()

        return DxlConnectionDeclaration(typeRef, connectedConcept, validTime, properties)

    }

    /**
     * connectivityDeclaration
     *   : concept (connectionDeclaration | "no-longer" disconnectionDeclaration)?
     *   ;
     */
    private fun parseConnectivityDeclaration(documentation: DxlOptDocumentation): DxlConnectivityDeclaration {

        // concept
        val concept = parseConceptDeclaration()

        // (connectionDeclaration | "no-longer" disconnectionDeclaration)?
        val connection: DxlOptConnectionDeclaration = when {

            input.hasLookAhead(SEMICOLON) -> {
                DxlNoConnectionDeclaration
            }

            input.consumeWhen(NO_LONGER)  -> {
                parseDisconnectionDeclaration()
            }

            else                          -> {
                parseConnectionDeclaration()
            }

        }

        // ";"
        input.read(SEMICOLON)

        return DxlConnectivityDeclaration(documentation, concept, connection)

    }

    /**
     * disconnectionDeclaration
     *   : typeRef connectedConcept validTime?
     *   ;
     */
    private fun parseDisconnectionDeclaration(): DxlDisconnectionDeclaration {

        // typeRef
        val typeRef = DxlTypeRef(parseQualifiedName(), false)

        // connectedConcept
        val connectedConcept = parseConceptReference()

        // validTime?
        val validTime = parseValidTimeOpt()

        return DxlDisconnectionDeclaration(typeRef, connectedConcept, validTime)

    }

    /**
     * declarations
     *   : connectivityDeclaration*
     *   ;
     *   TODO: more declaration types?
     */
    private fun parseDeclarations(): DxlDeclarations {

        val declarations = mutableListOf<DxlDeclaration>()

        while (!input.hasLookAhead(END_OF_INPUT)) {

            val documentation = parseDocumentationOpt()
            val connectivityDeclaration = parseConnectivityDeclaration(documentation)

            declarations.add(connectivityDeclaration)

        }

        if (declarations.isEmpty()) {
            input.expected("Concept or connection declaration")
        }

        return DxlDeclarations(declarations)

    }

    /**
     * Parses one block of documentation.
     *
     * documentation
     *   : DOCUMENTATION
     *   ;
     */
    private fun parseDocumentation(): DxlDocumentation {

        // DOCUMENTATION
        val token = input.read(DOCUMENTATION)

        return DxlDocumentation(token.origin, token.text)

    }

    /**
     * Parses an optional block of documentation.
     */
    private fun parseDocumentationOpt(): DxlOptDocumentation {

        if (input.hasLookAhead(DOCUMENTATION)) {
            return parseDocumentation()
        }

        return DxlNoDocumentation

    }

    /**
     * Parses an expression.
     *
     * expression
     *   : "absent"
     *   | booleanLiteral
     *   | characterLiteral
     *   | dateLiteral
     *   | dateTimeLiteral
     *   | floatingPointLiteral
     *   | integerLiteral
     *   | stringLiteral
     *   | timeLiteral
     *   | urlLiteral
     *   | uuidLiteral
     *   ;
     */
    private fun parseExpression(): DxlExpression {

        val token = input.read()

        when (token.type) {
            ABSENT                 -> return DxlAbsent(token.origin)
            BOOLEAN_LITERAL        -> return DxlBooleanLiteral(token.origin, token.text)
            CHARACTER_LITERAL      -> return DxlCharacterLiteral(token.origin, token.text)
            DATE_LITERAL           -> return DxlDateLiteral(token.origin, token.text)
            DATE_TIME_LITERAL      -> return DxlDateTimeLiteral(token.origin, token.text)
            FLOATING_POINT_LITERAL -> return DxlFloatingPointLiteral(token.origin, token.text)
            INTEGER_LITERAL        -> return DxlIntegerLiteral(token.origin, token.text)
            STRING_LITERAL         -> return DxlStringLiteral(token.origin, token.text)
            TIME_LITERAL           -> return DxlTimeLiteral(token.origin, token.text)
            URL_LITERAL            -> return DxlUrlLiteral(token.origin, token.text)
            UUID_LITERAL           -> return DxlUuidLiteral(token.origin, token.text)
            else                   -> input.expected("expression")
        }

        // TODO: more than just literals

    }

    /**
     * conceptReference
     *   : (uuid | qualifiedName)?
     *   ;
     */
    private fun parseLabelOpt(): DxlOptLabel {

        // uuid?
        if (input.hasLookAhead(UUID_LITERAL)) {
            val uuidToken = input.read(UUID_LITERAL)
            return DxlUuidLabel(uuidToken.origin, uuidToken.toUuid)
        }

        // qualifiedName?
        if (input.hasLookAhead(SYMBOL_NAME)) {
            return parseQualifiedName()
        }

        return DxlNoLabel

    }

    /**
     * Parses an optional sequence of properties.
     *
     * properties:
     *   : "with" property ("," property)*
     *   ;
     */
    private fun parsePropertiesOpt(): DxlProperties {

        val properties = mutableListOf<DxlProperty>()

        // "with"
        if (input.consumeWhen(WITH)) {
            properties.add(parseProperty())
        }

        // property ("," property)*
        while (input.consumeWhen(COMMA)) {
            properties.add(parseProperty())
        }

        return DxlProperties(properties)

    }

    /**
     * Parses a property definition.
     *
     * property
     *   : simpleName "=" expression validTimeInterval? transactionTime?
     *   ;
     *
     */
    private fun parseProperty(): DxlProperty {

        // simpleName
        val name = parseSimpleName()

        // "="
        input.read(EQUALS)

        // expression
        val value = parseExpression()

        // validTimeInterval?
        val validTimeInterval = parseValidTimeIntervalOpt()

        // transactionTime?
        val transactionTime = parseTransactionTimeOpt()

        return DxlProperty(name, value, validTimeInterval, transactionTime)

    }

    /**
     * Parses a qualified name.
     *
     * qualifiedName
     *   : simpleName ("." simpleName)*
     *   ;
     */
    private fun parseQualifiedName(): DxlName {

        val names = mutableListOf<DxlSimpleName>()

        while (true) {

            // simpleName
            val simpleName = parseSimpleName()
            names.add(simpleName)

            // "."?
            if (!input.hasLookAhead(2, SYMBOL_NAME) || !input.consumeWhen(DOT)) {
                break
            }

        }

        if (names.size > 1) {
            return DxlQualifiedName(names)
        }

        return names[0]

    }

    /**
     * Parses a simple name (single symbol name).
     *
     * simpleName
     *   : SYMBOL_NAME
     *   ;
     */
    private fun parseSimpleName(): DxlSimpleName {

        // SYMBOL_NAME
        val nameToken = input.read(SYMBOL_NAME)

        return DxlSimpleName(nameToken.origin, nameToken.text)

    }

    /**
     * Parses an optional "transacted-at" phrase.
     *
     * transactionTime
     *   : "transacted-at" dateTimeLiteral
     *   ;
     */
    private fun parseTransactionTimeOpt(): Instant? {

        // "transacted-at"
        return if (input.consumeWhen(TRANSACTED_AT)) {
            // dateTimeLiteral
            input.read(DATE_TIME_LITERAL).toInstant
        }
        else {
            null
        }

    }

    /**
     * Parses an optional type reference.
     */
    private fun parseTypeRefOpt(): DxlOptTypeRef {

        if (!input.hasLookAhead(COLON)) {
            return DxlNoTypeRef
        }

        return parseTypeRef(true)
    }

    /**
     * Parses a simple name (single symbol name).
     *
     * typeRef
     *   : COLON qualifiedName
     *   ;
     */
    private fun parseTypeRef(isForNamedElement: Boolean): DxlTypeRef {

        // COLON
        input.read(COLON)

        // qualifiedName
        val typeName = parseQualifiedName()

        return DxlTypeRef(typeName, isForNamedElement)

    }

    /**
     * Parses an optional "stated-as-of" phrase.
     *
     * validTime
     *   : "valid-as-of" dateTimeLiteral
     *   ;
     */
    private fun parseValidTimeOpt(): Instant? {

        // "valid-as-of"
        return if (input.consumeWhen(VALID_AS_OF)) {
            // dateTimeLiteral
            input.read(DATE_TIME_LITERAL).toInstant
        }
        else {
            null
        }

    }

    /**
     * Parses an optional "valid-as-of" or "valid-during" phrase.
     *
     * validTime
     *   : "valid-as-of" dateTimeLiteral
     *   | "valid-during" dateTimeLiteral ".." dateTimeLiteral
     *   ;
     */
    private fun parseValidTimeIntervalOpt(): TimeInterval? {

        return when {

            // "valid-as-of"
            input.consumeWhen(VALID_AS_OF)  -> {
                // dateTimeLiteral
                TimeInterval.startingAt(input.read(DATE_TIME_LITERAL).toInstant)
            }

            // "valid-during"
            input.consumeWhen(VALID_DURING) -> {
                // dateTimeLiteral
                val start = input.read(DATE_TIME_LITERAL).toInstant
                // ".."
                input.read(DOUBLE_DOT)
                // dateTimeLiteral
                TimeInterval.of(start, input.read(DATE_TIME_LITERAL).toInstant)
            }

            else                            -> {
                null
            }

        }

    }

    ////

    /**
     * Adds the file name to a token origin.
     */
    private val DxlToken.origin
        get() = DxlFileOrigin(fileName, this.line, this.column)


    /**
     * Parses the date/time from a date/time literal token.
     */
    private val DxlToken.toInstant
        get() : Instant {
            require(this.type == DATE_TIME_LITERAL)
            return Instant.parse(this.text.substring(1, this.text.length - 1))
        }

    /**
     * Parses the UUID value from a UUID literal token.
     */
    private val DxlToken.toUuid
        get() : Uuid {
            require(this.type == UUID_LITERAL)
            return Uuid.fromString(this.text)
        }

}
