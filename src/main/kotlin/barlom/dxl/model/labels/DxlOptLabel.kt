//
// (C) Copyright 2019-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.labels

import barlom.dxl.model.core.DxlItem
import barlom.dxl.model.core.DxlNullOrigin
import barlom.dxl.model.core.DxlOrigin
import barlom.util.Uuid

//---------------------------------------------------------------------------------------------------------------------

sealed class DxlOptLabel(
    origin: DxlOrigin
) : DxlItem(origin)

//---------------------------------------------------------------------------------------------------------------------

object DxlNoLabel
    : DxlOptLabel(DxlNullOrigin)

//---------------------------------------------------------------------------------------------------------------------

class DxlStringLabel(
    origin: DxlOrigin,
    val text: String
) : DxlOptLabel(origin)

//---------------------------------------------------------------------------------------------------------------------

class DxlUuidLabel(
    origin: DxlOrigin,
    val uuid: Uuid
) : DxlOptLabel(origin)

//---------------------------------------------------------------------------------------------------------------------

sealed class DxlName(
    origin: DxlOrigin
) : DxlOptLabel(origin) {

    abstract val text: String

}

//---------------------------------------------------------------------------------------------------------------------

class DxlSimpleName(
    origin: DxlOrigin,
    val name: String
) : DxlName(origin) {

    override val text = name

}

//---------------------------------------------------------------------------------------------------------------------

class DxlQualifiedName(
    val names: List<DxlSimpleName>
) : DxlName(if (names.isNotEmpty()) names[0].origin else throw IllegalArgumentException("")) {

    override val text =
        names.joinToString(".") { n -> n.name }

}

//---------------------------------------------------------------------------------------------------------------------

