//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.declarations

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.properties.DxlProperties

//---------------------------------------------------------------------------------------------------------------------

/**
 * Declaration of a concept (node/vertex) of a concept graph (with its properties).
 */
class DxlConceptDeclaration(

    /** The concept name, UUID, type, etc. */
    val reference: DxlConceptReference,

    /** The properties of the concept. */
    val properties: DxlProperties

) : DxlItem(reference.origin)

//---------------------------------------------------------------------------------------------------------------------

