package com.github.scormaq.rp

import cucumber.api.Result
import cucumber.runtime.table.TablePrinter
import gherkin.ast.DataTable
import gherkin.ast.Examples
import gherkin.ast.Node
import java.lang.System.lineSeparator

// table printer with disabled indent
private object NoIndentTablePrinter : TablePrinter() {
    override fun printStartIndent(buffer: java.lang.StringBuilder?, rowIndex: Int) {}
}

/**
 * Replaces Examples parameters in table cells
 */
fun resolveTable(stepArgument: Node, example: Examples, rowIndex: Int): String? =
    (stepArgument as DataTable).let { arg ->
        val sb = StringBuilder()
         val exampleParams = getExampleParams(example, rowIndex)
        val resolvedTable = arg.rows.map { row ->
            row.cells.map { it.value }.map { exampleParams[it.trimDelimiters()] ?: it }
        }
        NoIndentTablePrinter.printTable(resolvedTable, sb)
        sb.toString()
    }

private fun String.trimDelimiters() = this.removePrefix("<").removeSuffix(">")

fun printTable(stepArgument: Node): String? = (stepArgument as? DataTable)?.let { arg ->
    val sb = StringBuilder()
    NoIndentTablePrinter.printTable(arg.rows.map { it.cells.map { cell -> cell.value } }, sb)
    sb.toString()
}

/**
 * for easy Feature result calculation: result1 + result2 = result3.
 * Obviously, accumulated result can only degrade - from PASSED to FAILED
 */
internal operator fun Result.Type.plus(other: Result.Type): Result.Type {
    return when {
        this == other -> this
        (this == Result.Type.PASSED) && (other in (listOf(Result.Type.SKIPPED, Result.Type.UNDEFINED))) -> Result.Type.PASSED
        (this == Result.Type.FAILED) || (other == Result.Type.FAILED) -> Result.Type.FAILED
        this == Result.Type.SKIPPED && other == Result.Type.FAILED -> Result.Type.FAILED
        else -> Result.Type.UNDEFINED
    }
}

// print feature uri as relative path to root features directory
fun getFeatureRelativePath(uri: String?) = uri?.substringAfter("features/")

/**
 * Returns current example row parameter values, mapped to parameter names
 */
fun getExampleParams(example: Examples, rowIndex: Int) = example.tableBody[rowIndex].cells.withIndex().map { (i, cell) ->
    example.tableHeader.cells[i].value to cell.value
}.toMap()

/**
 * Example parameters description for given test item
 */
fun describeExampleRow(example: Examples, rowIndex: Int) =
    getExampleParams(example, rowIndex).entries.joinToString(lineSeparator()) { "${it.key} : ${it.value}" }