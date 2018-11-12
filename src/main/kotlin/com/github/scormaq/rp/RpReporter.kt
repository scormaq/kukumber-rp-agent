package com.github.scormaq.rp

import com.epam.reportportal.service.*
import com.epam.ta.reportportal.ws.model.*
import com.epam.ta.reportportal.ws.model.launch.*
import cucumber.api.Result
import cucumber.api.TestCase
import cucumber.api.event.TestStepStarted
import gherkin.ast.ScenarioDefinition
import gherkin.ast.Tag
import io.reactivex.Maybe
import org.slf4j.*
import java.util.Calendar

object RpReporter {

    private enum class TestItemType { SUITE, SCENARIO, TEST }

    private val LOGGER = LoggerFactory.getLogger(RpReporter::class.java)

    private var currentFeatureRq: Maybe<String>? = null
    private var currentScenarioRq: Maybe<String>? = null
    private var currentExampleRq: Maybe<String>? = null

    private var currentFeatureFile: String = ""

    private val launch: Launch by lazy {
        val reportPortal = ReportPortal.builder().build()
        val parameters = reportPortal.parameters

        val launchRq = StartLaunchRQ()
        launchRq.name = parameters.launchName
        launchRq.startTime = Calendar.getInstance().time
        launchRq.mode = parameters.launchRunningMode
        launchRq.tags = parameters.tags
        launchRq.description = parameters.description

        reportPortal.newLaunch(launchRq)
    }

    fun startLaunch() {
        launch.start()
    }

    fun startFeature(testCase: TestCase) {
        currentFeatureFile = testCase.uri
        val feature = TestSourcesModel.getFeature(currentFeatureFile)
        currentFeatureRq = startItem(initStartTestItemRQ = {
            description = "${feature?.keyword}: ${feature?.name}" +
                // apply cucumber feature description if available
                (feature?.description?.let { "\n\n$it" } ?: "")
            name = getFeatureRelativePath(currentFeatureFile)
            tags = extractTags(feature?.tags)
            startTime = Calendar.getInstance().time
            type = TestItemType.SUITE.name
        })
    }

    fun <T : ScenarioDefinition> startScenario(scenario: T, scenarioTags: MutableCollection<Tag>?) {
        currentScenarioRq = startItem(currentFeatureRq) {
            description = "$currentFeatureFile:${scenario.location.line}" +
                // apply cucumber scenario description if available
                (scenario.description?.let { "\n\n$it" } ?: "")
            name = "${scenario.keyword}: ${scenario.name}"
            tags = extractTags(scenarioTags)
            startTime = Calendar.getInstance().time
            type = TestItemType.SCENARIO.name
        }
    }

    fun startExampleRow(outlineRows: OutlineRows) {
        val example = outlineRows.getCurrentExamples()
        currentExampleRq = startItem(currentScenarioRq) {
            description = describeExampleRow(example, outlineRows.currentExampleRowIndex)
            name = "Example #${outlineRows.newTotalRowIndex()}"
            tags = extractTags(example.tags)
            startTime = Calendar.getInstance().time
            type = TestItemType.TEST.name
        }
    }

    fun logHook(event: TestStepStarted) {
        val message: String = event.testStep.run { "${hookType.name}: $codeLocation" }
        sendLog(text = message, logLevel = LogLevel.DEBUG)
    }

    fun logStep(event: TestStepStarted, outlineRows: OutlineRows?) {
        val step = TestSourcesModel.getStep(currentFeatureFile, event)
        var definition = "${step.keyword}${event.testStep.stepText}"
        step.argument?.let { dataTable ->
            definition += System.lineSeparator()
            if (outlineRows != null) outlineRows.let {
                definition += resolveTable(dataTable, it.getCurrentExamples(), it.currentExampleRowIndex)
            } else definition += printTable(dataTable)
        }
        sendLog(text = definition, logLevel = LogLevel.INFO)
    }

    private fun startItem(rootItem: Maybe<String>? = null, initStartTestItemRQ: (StartTestItemRQ.() -> Unit)): Maybe<String> {
        return StartTestItemRQ().apply(initStartTestItemRQ).let { launch.startTestItem(rootItem, it) }
    }

    fun finishExample(status: Result.Type) {
        finishTestItem(currentExampleRq, status)
        currentExampleRq = null
    }

    fun finishScenario(status: Result.Type) {
        finishTestItem(currentScenarioRq, status)
        currentScenarioRq = null
    }

    fun finishFeature(status: Result.Type) {
        finishTestItem(currentFeatureRq, status)
        currentFeatureRq = null
    }

    private fun finishTestItem(itemId: Maybe<String>?, cucumberStatus: Result.Type) {
        itemId?.let {
            val rq = FinishTestItemRQ()
            rq.status = mapCucumberStatus(cucumberStatus)
            rq.endTime = Calendar.getInstance().time
            launch.finishTestItem(it, rq)
        } ?: LOGGER.error("Error while trying to finish ReportPortal test item!")
    }

    fun finishLaunch() {
        val finishLaunchRq = FinishExecutionRQ()
        finishLaunchRq.endTime = Calendar.getInstance().time
        launch.finish(finishLaunchRq)
    }
}