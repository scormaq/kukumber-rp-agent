package com.github.scormaq.rp

import gherkin.ast.Examples

/**
 * Aggregates multiple Examples sections into 1 object for same ScenarioOutline
 */
class OutlineRows(
        private var totalExamples: MutableList<Examples> = mutableListOf(),
        private var examplesIndex: Int = 0,
        var currentExampleRowIndex: Int = 0,
        private var totalExampleRowsIndex: Int = 0,
        private var totalExampleRowsCount: Int = totalExamples.sumBy { it.tableBody.size }) {

    fun getCurrentExamples(): Examples = totalExamples[examplesIndex]

    fun newTotalRowIndex() = ++totalExampleRowsIndex

    fun countFinishedExampleRows() {
        currentExampleRowIndex += 1
        if (currentExampleRowIndex == totalExamples[examplesIndex].tableBody.size) {
            currentExampleRowIndex = 0
            examplesIndex += 1
        }
    }

    fun areAllRowsFinished(): Boolean = (totalExampleRowsCount == totalExampleRowsIndex)
}