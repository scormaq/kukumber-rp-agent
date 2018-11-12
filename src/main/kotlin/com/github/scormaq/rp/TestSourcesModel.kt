package com.github.scormaq.rp

import cucumber.api.TestCase
import cucumber.api.event.TestSourceRead
import cucumber.api.event.TestStepStarted
import gherkin.AstBuilder
import gherkin.Parser
import gherkin.ParserException
import gherkin.TokenMatcher
import gherkin.ast.Background
import gherkin.ast.Feature
import gherkin.ast.GherkinDocument
import gherkin.ast.Node
import gherkin.ast.ScenarioDefinition
import gherkin.ast.ScenarioOutline
import gherkin.ast.Step
import java.util.HashMap

/**
 * Suitable copy of class cucumber.runtime.formatter.TestSourcesModel
 */
internal object TestSourcesModel {

    private val pathToReadEventMap = HashMap<String, TestSourceRead>()
    private val pathToAstMap = HashMap<String, GherkinDocument>()
    private val pathToNodeMap = HashMap<String, Map<Int, AstNode>>()

    internal fun addTestSourceReadEvent(event: TestSourceRead) = pathToReadEventMap.put(event.uri, event)

    fun getFeature(path: String): Feature? {
        if (!pathToAstMap.containsKey(path)) {
            parseGherkinSource(path)
        }
        return if (pathToAstMap.containsKey(path)) {
            pathToAstMap[path]?.feature
        } else null
    }

    fun getScenarioDefinition(featureFilePath: String, testCase: TestCase): ScenarioDefinition {
        val astNode: AstNode? = getAstNode(featureFilePath, testCase.line)
        return astNode?.node as? ScenarioDefinition ?: astNode?.parent!!.parent!!.node as ScenarioDefinition
    }

    fun getStep(featureFilePath: String, event: TestStepStarted): Step {
        return getAstNode(featureFilePath, event.testStep.stepLine)?.node as Step
    }

    fun isBackgroundStep(featureFilePath: String, event: TestStepStarted): Boolean {
        return getAstNode(featureFilePath, event.testStep.stepLine)?.parent?.node is Background
    }

    private fun getAstNode(path: String?, line: Int): AstNode? {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path)
        }
        return if (pathToNodeMap.containsKey(path)) {
            pathToNodeMap[path]?.get(line)
        } else null
    }

    private fun parseGherkinSource(path: String?) {
        if (!pathToReadEventMap.containsKey(path)) {
            return
        }
        val parser = Parser(AstBuilder())
        val matcher = TokenMatcher()
        path?.let {
            try {
                val gherkinDocument = parser.parse(pathToReadEventMap[it]?.source, matcher)
                pathToAstMap[it] = gherkinDocument
                val nodeMap = HashMap<Int, AstNode>()
                val currentParent = AstNode(gherkinDocument.feature, null)
                for (child in gherkinDocument.feature.children) {
                    processScenarioDefinition(nodeMap, child, currentParent)
                }
                pathToNodeMap.put(it, nodeMap)
            } catch (e: ParserException) {
                // Ignore exceptions
            }
        }
    }

    private fun processScenarioDefinition(nodeMap: MutableMap<Int, AstNode>, child: ScenarioDefinition, currentParent: AstNode) {
        val childNode = AstNode(child, currentParent)
        nodeMap[child.location.line] = childNode
        child.steps.forEach { nodeMap[it.location.line] = AstNode(it, childNode) }
        if (child is ScenarioOutline) {
            processScenarioOutlineExamples(nodeMap, child, childNode)
        }
    }

    private fun processScenarioOutlineExamples(nodeMap: MutableMap<Int, AstNode>, scenarioOutline: ScenarioOutline, childNode: AstNode) {
        for (examples in scenarioOutline.examples) {
            val examplesNode = AstNode(examples, childNode)
            val headerRow = examples.tableHeader
            val headerNode = AstNode(headerRow, examplesNode)
            nodeMap[headerRow.location.line] = headerNode
            for (i in 0 until examples.tableBody.size) {
                val examplesRow = examples.tableBody[i]
                val rowNode = ExamplesRowWrapperNode(examplesRow)
                val expandedScenarioNode = AstNode(rowNode, examplesNode)
                nodeMap[examplesRow.location.line] = expandedScenarioNode
            }
        }
    }
}

class AstNode(val node: Node, val parent: AstNode?)

internal class ExamplesRowWrapperNode(examplesRow: Node) : Node(examplesRow.location)
