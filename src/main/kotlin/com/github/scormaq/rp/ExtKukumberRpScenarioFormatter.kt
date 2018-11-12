package com.github.scormaq.rp

import java.io.File

class ExtKukumberRpScenarioFormatter : KukumberRpScenarioFormatter() {

    override fun getFailureData(): File? = takeScreenshot()

    private fun takeScreenshot() = File("src/test/resources/test_screenshot.png")
}