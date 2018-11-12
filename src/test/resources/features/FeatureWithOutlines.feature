@beverage

Feature: Look at menu and order a beverage

  Demonstrate reporting to RP cucumber Scenario outline with Examples

  Scenario Outline: Look at menu a few times

  This scenario outline contains only 1 Examples section

    Given I see next beverage in my menu:
      | not parameterized | item name | item price | qty available |
      | sample text       | <val 1>   | <val 2>    | <val 3>       |

    Examples:
      | val 1      | val 2  | val 3 |
      | tea        | $4.99  | 99    |
      | soda       | $7.99  | 10    |
      | cappuccino | $99.99 | 0     |


  Scenario Outline: Order some refreshing drink (not latte)

  This scenario outline contains a few Examples tables.
  Each table also have different tag.

    Given I am thirsty
    When I order a <ordered>
    Then I should receive a <expected>, not latte

  @foobar
    Examples:
      | ordered           | expected   |
      | soda              | soda       |
      | totally not latte | latte      |
      | cappuccino        | cappuccino |

  @barbaz
    Examples:
      | ordered | expected |
      | milk    | milk     |
      | tea     | tea      |


