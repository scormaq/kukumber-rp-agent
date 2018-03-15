package com.github.scormaq.cucumber.definitions

import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.Given
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(HookDefinitions::class.java)

class HookDefinitions {

    @Before(value = ["@milk"], order = 10)
    fun beforeMilkHook10() {
        Thread.sleep(100)
    }

    @Before(value = ["@milk"], order = 11)
    fun beforeMilkHook11() {
        Thread.sleep(100)
    }

    @Before(value = ["@cappuccino"], order = 11)
    fun beforeCappuccino() {
        Thread.sleep(100)
    }

    @Before(value = ["@milk", "@cappuccino"])
    fun beforeMilkAndCappuccino() {
        Thread.sleep(100)
    }


    @Given("^I am going to order a refreshing drink (.+)$")
    fun prepareDrink(beverage: String) {
        LOGGER.info("Ordering a $beverage")
        Thread.sleep(100)
    }

    @After(value = ["cappuccino"], order = 11)
    fun afterCappuccino() {
        Thread.sleep(100)
    }

    @After(value = ["@milk", "@cappuccino"], order = 11)
    fun afterMilkAndCappuccino() {
        Thread.sleep(100)
    }

    @After(value = ["@hooks_in_action"])
    fun afterAny() {
        LOGGER.warn("AFTER ANY")
        Thread.sleep(100)
    }

}