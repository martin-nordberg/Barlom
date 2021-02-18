//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.declarations

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlNullOrigin
import barlom.dxl.model.core.DxlOrigin
import barlom.dxl.model.labels.*
import barlom.dxl.model.types.DxlOptTypeRef

//---------------------------------------------------------------------------------------------------------------------

/**
 * An optional concept.
 */
sealed class DxlOptConceptReference(

    /** The starting origin of the concept. */
    origin: DxlOrigin

) : DxlItem(origin)

//---------------------------------------------------------------------------------------------------------------------

/**
 * Null object representing an optional concept that has been omitted (generally for an intransitive connection).
 */
object DxlNoConceptReference
    : DxlOptConceptReference(DxlNullOrigin)

//---------------------------------------------------------------------------------------------------------------------

/**
 * Declaration of a concept (node/vertex) of a concept graph (without its properties).
 */
class DxlConceptReference(

    /** The label for the concept. */
    val label: DxlOptLabel,

    /** The type of the concept. */
    val typeRef: DxlOptTypeRef

) : DxlOptConceptReference(if (label is DxlNoLabel) typeRef.origin else label.origin)

//---------------------------------------------------------------------------------------------------------------------

