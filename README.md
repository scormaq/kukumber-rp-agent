### Kukumber-rp-agent
Kotlin implementation of ReportPortal agent for new Cucumber test model (Cucumber 2.x)

### How to test
To see how agent works, do next:
1. Set up `src/main/resources/reportportal.properties`;
2. Make sure your main test class (in this project - `com.github.scormaq.cucumber.CucumberTestSuite`) contains agent, applied as plugin; 
3. Run `gradlew test` (be aware that tests contain 1 failure for demo purpose);
4. Go to your ReportPortal instance and see how launch was reported;
5. Use `com.github.scormaq.rp.ExtKukumberRpFormatter` instead of `com.github.scormaq.rp.KukumberRpFormatter` to see how screenshots are reported

### Sending files
Agent is capable to send files to ReportPortal for different Cucumber events (for now - sending files when step failed). To enable sending files (e.g. screenshots for UI tests) use as plugin your own extension of class `com.github.scormaq.rp.KukumberRpFormatter` with implemented method `fun getFailureData(): File?`

#### Miscellaneous notes
* Reporting logs is out of scope of this agent (log4j2 ReportPortal appender used here for testing - refer to `src/main/resources/log4j2.xml` for configuration example);
* While sending screenshots make sure property `rp.convertimage` is set to `false`, otherwise ReportPortal client fails to emit log data

#### Links
Find more documentation at:
* https://cucumber.io/ 
* http://reportportal.io/
* https://kotlinlang.org/
* https://www.google.com/