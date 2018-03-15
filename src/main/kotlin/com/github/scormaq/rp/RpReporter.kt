package com.github.scormaq.rp

import com.epam.reportportal.listeners.Statuses
import com.epam.reportportal.service.Launch
import com.epam.reportportal.service.ReportPortal
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ
import com.epam.ta.reportportal.ws.model.StartTestItemRQ
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ
import cucumber.api.Result
import cucumber.api.TestCase
import cucumber.api.event.TestStepStarted
import gherkin.ast.ScenarioDefinition
import gherkin.ast.Tag
import io.reactivex.Maybe
import org.slf4j.LoggerFactory
import java.util.*

internal object RpReporter {

    private val LOGGER = LoggerFactory.getLogger(RpReporter::class.java)

    private var currentFeatureRq: Maybe<String>? = null
    private var currentScenarioRq: Maybe<String>? = null
    private var currentExampleRq: Maybe<String>? = null
    private var currentStepRq: Maybe<String>? = null

    private var currentFeatureUri: String = ""

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
        currentFeatureUri = testCase.uri
        val feature = TestSourcesModel.getFeature(currentFeatureUri)
        currentFeatureRq = startItem(initStartTestItemRQ = {
            description = "${feature?.keyword}: ${feature?.name}${feature?.description?.let { "\n\n$it" } ?: ""}"
            name = getFeatureRelativePath(currentFeatureUri)
            tags = extractTags(feature?.tags)
            startTime = Calendar.getInstance().time
            type = "SUITE"
        })
    }

    fun <T : ScenarioDefinition> startScenario(scenario: T, scenarioTags: MutableCollection<Tag>?) {
        currentScenarioRq = startItem(currentFeatureRq, {
            description = "$currentFeatureUri:${scenario.location.line}${scenario.description?.let { "\n\n$it" } ?: ""}"
            name = "${scenario.keyword}: ${scenario.name}"
            tags = extractTags(scenarioTags)
            startTime = Calendar.getInstance().time
            type = "SCENARIO"
        })
    }

    fun startExampleRow(outlineRows: OutlineRows?) {
        val example = outlineRows?.getCurrentExamples()!!
        currentExampleRq = startItem(currentScenarioRq, {
            description = describeExampleRow(example, outlineRows.currentExampleRowIndex)
            name = "Example #${outlineRows.newTotalRowIndex()}"
            tags = extractTags(example.tags)
            startTime = Calendar.getInstance().time
            type = "TEST"
        })
    }

    // parent test item item is either example (if scenario is Outline) or scenario
    fun startStep(event: TestStepStarted) {
        val step = TestSourcesModel.getStep(currentFeatureUri, event)
        val descriptionPrefix = if (TestSourcesModel.isBackgroundStep(currentFeatureUri, event)) "Background: " else ""
        currentStepRq = startItem(currentExampleRq ?: currentScenarioRq, {
            name = "$descriptionPrefix${step.keyword} ${event.testStep.stepText}"
            description = prettyPrintTable(step)
            startTime = Calendar.getInstance().time
            type = "STEP"
        })
    }

    fun startHook(event: TestStepStarted) {
        val hookStep = TestSourcesModel.getHookStep(event)
        currentStepRq = startItem(currentExampleRq ?: currentScenarioRq, {
            name = "${hookStep.hookType.name}: ${event.testStep.codeLocation}"
            startTime = Calendar.getInstance().time
            type = "STEP"
        })
    }

    private fun startItem(rootItem: Maybe<String>? = null, initStartTestItemRQ: (StartTestItemRQ.() -> Unit)): Maybe<String> {
        return StartTestItemRQ().apply(initStartTestItemRQ).let { launch.startTestItem(rootItem, it) }
    }

    fun finishStep(status: Result.Type) {
        finishTestItem(currentStepRq, status)
        currentStepRq = null
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

    fun finishLaunch() {
        val finishLaunchRq = FinishExecutionRQ()
        finishLaunchRq.endTime = Calendar.getInstance().time
        launch.finish(finishLaunchRq)
    }

    private fun finishTestItem(itemId: Maybe<String>?, cucumberStatus: Result.Type) {
        itemId?.let {
            val rq = FinishTestItemRQ()
            rq.status = mapCucumberStatus(cucumberStatus)
            rq.endTime = Calendar.getInstance().time
            launch.finishTestItem(it, rq)
        } ?: LOGGER.error("Error while trying to finish ReportPortal test item!")
    }

    private fun extractTags(tags: MutableCollection<Tag>?): HashSet<String>? = tags?.map { it.name }?.toHashSet()

    private fun mapCucumberStatus(cucumberStatus: Result.Type): String {
        return when (cucumberStatus) {
            Result.Type.PASSED -> Statuses.PASSED
            Result.Type.SKIPPED -> Statuses.SKIPPED
            Result.Type.FAILED -> Statuses.FAILED
            else -> Statuses.FAILED
        }
    }
}