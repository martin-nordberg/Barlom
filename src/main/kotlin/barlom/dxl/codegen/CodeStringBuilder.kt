//
// (C) Copyright 2019-2020 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.dxl.codegen

//---------------------------------------------------------------------------------------------------------------------

/**
 * Wraps a string builder to support indentation.
 */
class CodeStringBuilder(
    private val indentWhiteSpace: String = "  "
) {

    /** The code output so far, except for the latest line under construction. */
    private val builder = StringBuilder()

    /** The code of the last line being output. */
    private val currentLine = StringBuilder()

    /** The current indentation level. */
    private var indentationLevel = 0

    /** The white space corresponding to the current indentation level. */
    private var indentSpaces = ""

    ////

    /**
     * Moves the current line from its holding point to the string builder output.
     */
    private fun acceptCurrentLine() {

        // Capture the current line from its builder.
        val code = currentLine.toString().trimEnd(' ', '\t')
        currentLine.clear()

        // Move the current line into the finished output.
        builder.append(code)

    }

    /**
     * Appends a given [snippet] to the output code. Handles indentation for new lines.
     */
    fun append(snippet: String) {

        var lineStartIndex = 0
        var lineEndIndex = snippet.indexOf('\n')

        while (true) {

            // Once no new line characters remain, complete simpler output then quit.
            if (lineEndIndex < 0) {
                val remainder = snippet.substring(lineStartIndex)
                appendToCurrentLine(remainder)
                break
            }

            // Determine the next line of output.
            val line = snippet.substring(lineStartIndex, lineEndIndex)

            // Move things from the current line to the main string builder; clear out the current line.
            appendToCurrentLine(line)

            // Move the current line into the finished output.
            acceptCurrentLine()
            builder.append('\n')

            // Advance to the next line.
            lineStartIndex = lineEndIndex + 1
            lineEndIndex = snippet.indexOf('\n', lineStartIndex)

        }

    }

    /**
     * Appends an explicit new line to the output.
     */
    fun appendNewLine() =
        append("\n")

    /**
     * Appends a [fragment] of code (known to have no new line characters) to the current line.
     */
    private fun appendToCurrentLine(fragment: String) {

        // Indent if the line is empty and the added code is not.
        if (currentLine.isEmpty() && fragment.trimEnd(' ', '\t').isNotEmpty()) {
            currentLine.append(indentSpaces)
            currentLine.append(fragment.trimStart(' ', '\t'))
        } else {
            currentLine.append(fragment)
        }

    }

    /**
     * Increments the indentation level by one for output occurring after this call.
     */
    private fun indent() {
        indentationLevel += 1
        indentSpaces += indentWhiteSpace
    }

    /**
     * Executes the given [output] with the indentation level increased by one.
     * @param output the closure to call for indented output.
     */
    fun indented(output: CodeStringBuilder.() -> Unit) {
        this.indent()
        try {
            this.output()
        } finally {
            this.unindent()
        }
    }

    /**
     * Converts the code builder output to a simple string.
     */
    override fun toString(): String {

        var result = builder.toString()

        if (currentLine.isNotEmpty()) {
            result += currentLine.toString().trimEnd(' ', '\t')
        }

        result = result.trimEnd('\n') + '\n'

        return result

    }

    /**
     * Decrements the indentation level by one.
     */
    private fun unindent() {

        check(this.indentationLevel > 0)

        indentationLevel -= 1

        indentSpaces = ""
        for (i in 1..indentationLevel) {
            indentSpaces += indentWhiteSpace
        }

        if (currentLine.isBlank()) {
            currentLine.clear()
        }

    }

}

//---------------------------------------------------------------------------------------------------------------------

