package bdd;

import io.cucumber.java8.En;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class StepDefinitions implements En {

    private String helloWorld;

    public StepDefinitions() {

        Given("I want to test that the cucumber setup works", () -> {
            helloWorld = "Hello World!!";
        });

        When("I execute the scenario", () -> {
            System.out.println("running...");
        });

        Then("I should see the Output {string}", (String string) -> {
            assertThat(helloWorld).isEqualTo(string);
        });
    }
}
