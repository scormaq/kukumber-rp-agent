package com.github.scormaq.cucumber.definitions

import cucumber.api.Scenario
import cucumber.api.java.Before
import cucumber.api.java.en.Given
import java.io.File

class AttachDefinitions {

    private lateinit var scenario: Scenario

    @Before(value = ["@attachments"])
    fun storeScenario(scenario: Scenario) {
        this.scenario = scenario
    }

    @Given("^I am using PDF file (\\S+) in my test$")
    fun usePdfFileInTest(file: File) {
        Thread.sleep(100)
        scenario.embed(file.readBytes(), "application/pdf")
    }

    @Given("^I am using ZIP file (\\S+) in my test$")
    fun useZipFileInTest(file: File) {
        Thread.sleep(100)
        scenario.embed(file.readBytes(), "application/zip")
    }
}