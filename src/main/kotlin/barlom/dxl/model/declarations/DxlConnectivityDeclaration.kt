//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.declarations

import barlom.dxl.model.documentation.DxlOptDocumentation


//---------------------------------------------------------------------------------------------------------------------

/**
 * A declaration of related concepts and connections.
 */
class DxlConnectivityDeclaration(

    /** Optional documentation for the concept. */
    documentation: DxlOptDocumentation,

    /** The primary concept of the declaration. */
    val concept: DxlConceptDeclaration,

    /** The connection declaration. */
    val connection: DxlOptConnectionDeclaration

) : DxlDeclaration(concept.origin, documentation)

//---------------------------------------------------------------------------------------------------------------------

