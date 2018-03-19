Feature: See how files attached in ReportPortal

  @attachments
  Scenario: Generate attachments by Cucumber

  Sending files to Report Portal using Cucumber embed events

    Given I am using PDF file src/test/resources/test_pdf.pdf in my test
    And I am using ZIP file src/test/resources/sample.7z in my test
