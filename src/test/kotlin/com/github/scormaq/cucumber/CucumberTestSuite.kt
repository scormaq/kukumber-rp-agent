package com.github.scormaq.cucumber

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith


@RunWith(Cucumber::class)
@CucumberOptions(
        features = ["classpath:features"],
        plugin = ["com.github.scormaq.rp.KukumberRpScenarioFormatter"]
)
class CucumberTestSuite