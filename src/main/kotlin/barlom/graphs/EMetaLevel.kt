//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs

//---------------------------------------------------------------------------------------------------------------------

enum class EMetaLevel {

    SYSTEM_META_META,

    SYSTEM_META,

    USER_META,

    USER;

    fun nextLevelDown(): EMetaLevel {
        return when (this) {
            SYSTEM_META_META -> SYSTEM_META
            SYSTEM_META      -> USER_META
            USER_META        -> USER
            else             -> throw IllegalStateException("User level concepts cannot serve as types.")
        }
    }

}

//---------------------------------------------------------------------------------------------------------------------
