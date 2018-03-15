### Kukumber-rp-agent
Kotlin implementation of ReportPortal agent for new Cucumber test model (Cucumber 2.x)

### How to test
To see how agent works, do next:
1. Set up `src/main/resources/reportportal.properties`
2. Run `gradlew test` (! tests contain 1 failure for demo purposes)
3. Go to your ReportPortal instance and see how launch was reported

#### Miscellaneous
* Attaching screenshots currently not implemented
* Reporting logs is out of scope of this agent (log4j2 ReportPortal appender used here for testing - refer to `src/main/resources/log4j2.xml` for configuration example)