package com.github.scormaq.cucumber.definitions

import cucumber.api.DataTable
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.junit.Assert
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(StepDefinitions::class.java)

class StepDefinitions {

    @Given("^I am thirsty$")
    fun someGivenStep() {
        LOGGER.info("I want to drink")
        Thread.sleep(100)
    }

    @Given("^I see next beverage in my menu:$")
    fun checkMenu(table: DataTable) {
        LOGGER.info("Data table found:\n$table")
        Thread.sleep(100)
    }

    @When("^I order a (.+)$")
    fun orderABeverage(beverage: String) {
        LOGGER.info("I order a: $beverage right now")
        Thread.sleep(100)
    }

    @Then("^I should receive a (.+), not (.+)$")
    fun verifyLatteNotReceived(fst: String, snd: String) {
        LOGGER.warn("Incoming assertion that '$snd' is not '$fst'")
        Thread.sleep(100)
        Assert.assertNotEquals("Received beverage is wrong!", fst, snd)
    }
}