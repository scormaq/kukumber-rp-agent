@hooks_in_action

Feature: Demonstrate how Cucumber hooks are reported in RP

  Definition for this feature contains a few hooks with different tags.
  Not tagged hooks not defined, because they work globally.

  @milk
  Scenario: Ordering a Milk
    Given I am going to order a refreshing drink milk

  @cappuccino
  Scenario: Ordering a Cappuccino
    Given I am going to order a refreshing drink cappuccino

  @cappuccino @milk
  Scenario: Ordering a Frappuccino
    Given I am going to order a refreshing drink frappuccino

  @latte
  Scenario: Ordering a Latte
    Given I am going to order a refreshing drink latte

  Scenario: Ordering a Soda
    Given I am going to order a refreshing drink soda