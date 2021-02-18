//
// (C) Copyright 2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.scanning.impl

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StringTokenizerTest {

    @Test
    fun `A string tokenizer returns a word`() {

        val tokenizer = StringTokenizer("xyzpqr")

        tokenizer.markAndAdvance()
        tokenizer.advance()
        tokenizer.advance()

        val token = tokenizer.extractTokenFromMark("word", ::makeToken)

        assertEquals("word:'xyz'@1,1", token)
    }

}

fun makeToken(tokenType: String, text: String, line: Int, column: Int): String {
    return "$tokenType:'$text'@$line,$column"
}
