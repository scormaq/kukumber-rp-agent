package com.github.scormaq.rp

import cucumber.api.Result
import cucumber.api.TestCase
import cucumber.api.event.EmbedEvent
import cucumber.api.event.EventPublisher
import cucumber.api.event.TestCaseFinished
import cucumber.api.event.TestCaseStarted
import cucumber.api.event.TestRunFinished
import cucumber.api.event.TestRunStarted
import cucumber.api.event.TestSourceRead
import cucumber.api.event.TestStepFinished
import cucumber.api.event.TestStepStarted
import cucumber.api.formatter.Formatter
import gherkin.ast.Scenario
import gherkin.ast.ScenarioOutline
import java.io.File

open class KukumberRpScenarioFormatter : Formatter {

    private var currentFeatureFile: String = ""
    private var featureResult: Result.Type = Result.Type.PASSED
    private var isScenarioOutlineTest = false
    private var currentScenarioOutlineName: String = ""
    private var outlineRows: OutlineRows? = null
    private var isBackroundProcessing = false

    // Configure Cucumber events listener
    override fun setEventPublisher(publisher: EventPublisher) {
        publisher.registerHandlerFor(TestSourceRead::class.java) { TestSourcesModel.addTestSourceReadEvent(it) }
        publisher.registerHandlerFor(TestRunStarted::class.java) { handleTestRunStarted() }
        publisher.registerHandlerFor(TestCaseStarted::class.java) { handleTestCaseStarted(it) }
        publisher.registerHandlerFor(TestStepStarted::class.java) { handleTestStepStarted(it) }
        publisher.registerHandlerFor(TestStepFinished::class.java) { handleTestStepFinished(it) }
        publisher.registerHandlerFor(EmbedEvent::class.java) { handleEmbed(it) }
        publisher.registerHandlerFor(TestCaseFinished::class.java) { handleTestCaseFinished(it) }
        publisher.registerHandlerFor(TestRunFinished::class.java) { handleTestRunFinished() }
    }

    private fun handleTestRunStarted() {
        RpReporter.startLaunch()
    }

    private fun handleTestCaseStarted(event: TestCaseStarted) {
        isScenarioOutlineTest = false
        handleFeature(event.testCase)
        handleScenarioOutline(event.testCase)
        handleScenario(event.testCase)
    }

    private fun handleFeature(testCase: TestCase) {
        if (currentFeatureFile.isEmpty()) {
            currentFeatureFile = testCase.uri
            RpReporter.startFeature(testCase)
        } else if (currentFeatureFile != testCase.uri) {
            initNextFeature(testCase)
        }
    }

    private fun initNextFeature(testCase: TestCase) {
        RpReporter.finishFeature(featureResult)
        featureResult = Result.Type.PASSED
        currentFeatureFile = testCase.uri
        RpReporter.startFeature(testCase)
    }

    private fun handleScenarioOutline(testCase: TestCase) {
        (TestSourcesModel.getScenarioDefinition(currentFeatureFile, testCase) as? ScenarioOutline)?.let { scenario ->
            isScenarioOutlineTest = true
            if (currentScenarioOutlineName != scenario.name) {
                currentScenarioOutlineName = scenario.name
                RpReporter.startScenario(scenario, scenario.tags)
                outlineRows = OutlineRows(scenario.examples)
            }
        }
    }

    private fun handleTestStepStarted(event: TestStepStarted) = when {
        event.testStep.isHook -> RpReporter.logHook(event)
        else -> {
            if (TestSourcesModel.isBackgroundStep(currentFeatureFile, event)) {
                if (!isBackroundProcessing) sendLog(text = "Background starts >>>", logLevel = LogLevel.TRACE)
                isBackroundProcessing = true
            } else if (isBackroundProcessing) {
                sendLog(text = ">>> Background ends", logLevel = LogLevel.TRACE)
                isBackroundProcessing = false
            }
            RpReporter.logStep(event, outlineRows)
        }
    }

    private fun handleEmbed(event: EmbedEvent) {
        createTempFile().apply {
            writeBytes(event.data)
            sendLog(text = "attachment", logLevel = LogLevel.DEBUG, file = this)
            println("${this.absolutePath} sent to RP")
            delete()
        }
    }

    private fun handleTestStepFinished(event: TestStepFinished) {
        if (event.result.`is`(Result.Type.FAILED)) {
            sendFailure(event.result, getFailureData())
        }
    }

    private fun handleScenario(testCase: TestCase) {
        val scenario = TestSourcesModel.getScenarioDefinition(currentFeatureFile, testCase)
        when (scenario) {
            is ScenarioOutline -> RpReporter.startExampleRow(outlineRows!!)
            is Scenario -> {
                isScenarioOutlineTest = false
                RpReporter.startScenario(scenario, scenario.tags)
            }
        }
    }

    /**
     * Provide implementation for taking files (e.g. screenshots) which are sent to Report Portal on step failure case
     */
    protected open fun getFailureData(): File? = null

    private fun handleTestCaseFinished(event: TestCaseFinished) {
        when (isScenarioOutlineTest) {
            true -> handleExampleRowFinished(event)
            false -> RpReporter.finishScenario(event.result.status)
        }
        featureResult += event.result.status
    }

    private fun handleExampleRowFinished(event: TestCaseFinished) {
        RpReporter.finishExample(event.result.status)
        outlineRows?.countFinishedExampleRows()
        if (outlineRows?.areAllRowsFinished() == true) {
            RpReporter.finishScenario(event.result.status)
            outlineRows = null
        }
    }

    private fun handleTestRunFinished() {
        RpReporter.finishFeature(featureResult)
        RpReporter.finishLaunch()
    }
}