package com.github.scormaq.rp


import cucumber.api.Result
import cucumber.api.Result.Type.PASSED
import cucumber.api.TestCase
import cucumber.api.event.*
import cucumber.api.formatter.Formatter
import gherkin.ast.Scenario
import gherkin.ast.ScenarioOutline

class KukumberRpFormatter : Formatter {

    private var currentFeatureFile: String = ""
    private var featureResult: Result.Type = PASSED
    private var isScenarioOutlineTest = false
    private var currentScenarioOutlineName: String = ""
    private var outlineRows: OutlineRows? = null

    // Configure Cucumber events listener
    override fun setEventPublisher(publisher: EventPublisher) {
        publisher.registerHandlerFor(TestSourceRead::class.java, { TestSourcesModel.addTestSourceReadEvent(it) })
        publisher.registerHandlerFor(TestRunStarted::class.java, { handleTestRunStarted() })
        publisher.registerHandlerFor(TestCaseStarted::class.java, { handleTestCaseStarted(it) })
        publisher.registerHandlerFor(TestStepStarted::class.java, { handleTestStepStarted(it) })
        publisher.registerHandlerFor(TestStepFinished::class.java, { handleTestStepFinished(it) })
        publisher.registerHandlerFor(TestCaseFinished::class.java, { handleTestCaseFinished(it) })
        publisher.registerHandlerFor(TestRunFinished::class.java, { handleTestRunFinished() })
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
        featureResult = PASSED
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

    private fun handleScenario(testCase: TestCase) {
        val scenario = TestSourcesModel.getScenarioDefinition(currentFeatureFile, testCase)
        when (scenario) {
            is ScenarioOutline -> RpReporter.startExampleRow(outlineRows)
            is Scenario -> {
                isScenarioOutlineTest = false
                RpReporter.startScenario(scenario, scenario.tags)
            }
        }
    }

    private fun handleTestStepStarted(event: TestStepStarted) {
        when {
            event.testStep.isHook -> RpReporter.startHook(event)
            else -> RpReporter.startStep(event)
        }
    }

    private fun handleTestStepFinished(event: TestStepFinished) {
        RpReporter.finishStep(event.result.status)
    }

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