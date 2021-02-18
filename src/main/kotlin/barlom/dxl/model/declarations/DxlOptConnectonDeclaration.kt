//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.declarations

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlNullOrigin
import barlom.dxl.model.core.DxlOrigin
import barlom.dxl.model.properties.DxlProperties
import barlom.dxl.model.types.DxlTypeRef
import java.time.Instant

//---------------------------------------------------------------------------------------------------------------------

/**
 * An optional connection from one concept to another.
 */
sealed class DxlOptConnectionDeclaration(

    /** The starting origin of the connection. */
    origin: DxlOrigin

) : DxlItem(origin)

//---------------------------------------------------------------------------------------------------------------------

/**
 * Null object representing an optional connection that has been omitted.
 */
object DxlNoConnectionDeclaration
    : DxlOptConnectionDeclaration(DxlNullOrigin)

//---------------------------------------------------------------------------------------------------------------------

/**
 * A disconnection declaration that is part of a concept declaration.
 */
class DxlDisconnectionDeclaration(

    /** The type of the connection. */
    val typeRef: DxlTypeRef,

    /** The connected concept. */
    val concept: DxlConceptReference,

    /* When the disconnection became valid. */
    val validTime: Instant?

) : DxlOptConnectionDeclaration(typeRef.origin)

//---------------------------------------------------------------------------------------------------------------------

/**
 * A connection declaration with its connected concept.
 */
class DxlConnectionDeclaration(

    /** The type of the connection. */
    val typeRef: DxlTypeRef,

    /** The connected concept. */
    val concept: DxlConceptReference,

    /* When the connection became valid. */
    val validTime: Instant?,

    /** The properties of the connection. */
    val properties: DxlProperties

) : DxlOptConnectionDeclaration(typeRef.origin)

//---------------------------------------------------------------------------------------------------------------------

/**
 * One concept linked to by a connection.
 */
class DxlConnectedConcept(

    /** The type of the connection. */
    val typeRef: DxlTypeRef,

    /** The connected concept. */
    val concept: DxlConceptReference,

    ) : DxlItem(typeRef.origin)

//---------------------------------------------------------------------------------------------------------------------

