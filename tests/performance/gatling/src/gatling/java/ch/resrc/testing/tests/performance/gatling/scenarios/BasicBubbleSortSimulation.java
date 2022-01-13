package ch.resrc.testing.tests.performance.gatling.scenarios;

import ch.resrc.testing.capabilities.json.JsonCapability;
import io.gatling.javaapi.core.*;

import java.security.SecureRandom;
import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

public class BasicBubbleSortSimulation extends Simulation {

    private final SortResource sortResource = new SortResource(
            new SecureRandom(),
            JsonCapability.json()
    );

    ScenarioBuilder basicBubbleSortScenario = scenario("Basic Bubble Sort")
            .exec(
                    repeat(2, sortResource.request("/basic-bubble-sort", 1)),
                    repeat(2, sortResource.request("/basic-bubble-sort", 10)),
                    repeat(2, sortResource.request("/basic-bubble-sort", 100)),
                    repeat(2, sortResource.request("/basic-bubble-sort", 1_000)),
                    repeat(2, sortResource.request("/basic-bubble-sort", 10_000))
            );

    {
        setUp(
                basicBubbleSortScenario.injectOpen(
                                rampUsers(20).during(2)
                        )
                        .protocols(
                                sortResource.httpProtocol()
                        )
        );
    }

    private ChainBuilder repeat(int times, ChainBuilder requestToRepeat) {
        return CoreDsl.repeat(times)
                .on(requestToRepeat)
                .pause(Duration.ofMillis(50));
    }
}
