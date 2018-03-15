package com.github.scormaq.cucumber.definitions

import cucumber.api.java.en.Given
import cucumber.api.java.en.Then


class BackgroundDefinitions {

    @Given("^test engineer walks into the bar$")
    fun immaBackgroundGivenStep() {
        Thread.sleep(100)
    }

    @Then("^menu should contain some refreshing beverages$")
    fun immaBackgroundThenStep() {
        Thread.sleep(100)
    }

}