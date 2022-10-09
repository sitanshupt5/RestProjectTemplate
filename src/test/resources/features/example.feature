Feature: User Registration

  Scenario Outline: User Post Details
    Given I have API "<API>"
    And I set the Content-Type as JSON
    And I set request body for "<RequestBody>"
    When I call method 'POST'
    Then I verify response code is 201
    Examples:
    |API              |RequestBody|
    |jsonplaceholder  | User1     |
    |jsonplaceholder  | User2     |
    |jsonplaceholder  | User3     |