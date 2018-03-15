@critical
Feature: Demonstrate how Background is repeatedly used for each scenario

  Background:
    Given test engineer walks into the bar
    Then menu should contain some refreshing beverages

  Scenario: Cappuccino Is Available In Menu
    Given I see next beverage in my menu:
      | item name  | item price | qty available |
      | cappuccino | $0.99      | 99            |

  Scenario: Latte Is Not Available In Menu
    Given I see next beverage in my menu:
      | item name | item price | qty available |
      | latte     | $8.99      | 0             |