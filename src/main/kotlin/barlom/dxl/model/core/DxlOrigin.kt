//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.model.core

//---------------------------------------------------------------------------------------------------------------------

/**
 * A record of where a parsed item originated.
 */
sealed class DxlOrigin

//---------------------------------------------------------------------------------------------------------------------

/**
 * Null object for when the origin is not relevant.
 */
object DxlNullOrigin
    : DxlOrigin() {

    override fun toString() =
        "[no origin]"

}

//---------------------------------------------------------------------------------------------------------------------

/**
 * An item originated at a particular line and column in a source file.
 */
data class DxlFileOrigin(

    /** The name of the source file. */
    val fileName: String,

    /** The line in the file (1-based). */
    val line: Int,

    /** The column in the file (1-based). */
    val column: Int

) : DxlOrigin() {

    override fun toString() =
        "$fileName($line,$column)"

}

//---------------------------------------------------------------------------------------------------------------------

