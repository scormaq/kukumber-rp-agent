package com.github.scormaq.rp

import cucumber.api.Result
import cucumber.runtime.table.TablePrinter
import gherkin.ast.DataTable
import gherkin.ast.Examples
import gherkin.ast.Step

// table printer with disabled indent
private object tablePrinter : TablePrinter() {
    override fun printStartIndent(buffer: java.lang.StringBuilder?, rowIndex: Int) {}
}

fun prettyPrintTable(step: Step): String? {
    return step.argument?.takeIf { it is DataTable }?.let { arg ->
        val sb = StringBuilder()
        tablePrinter.printTable((arg as DataTable).rows.map { it.cells.map { it.value } }, sb)
        sb.toString()
    }
}

// for easy Feature result calculation: result1 + result2 = result3
// obviously, accumulated result can only degrade - from PASSED to FAILED
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
fun getFeatureRelativePath(uri: String?) = uri?.substringAfter("/features/")

// print outline examples row for RP item description
fun describeExampleRow(example: Examples, rowIndex: Int): String {
    return (0..(example.tableHeader.cells.size - 1)).joinToString("\n") { i ->
        "${example.tableHeader.cells[i].value} = ${example.tableBody[rowIndex].cells[i].value}"
    }
}