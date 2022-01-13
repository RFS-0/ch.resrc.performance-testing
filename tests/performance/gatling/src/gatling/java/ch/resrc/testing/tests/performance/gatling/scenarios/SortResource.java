package ch.resrc.testing.tests.performance.gatling.scenarios;

import ch.resrc.testing.adapters.rest.endpoints.sort.dto.UnsortedListDto;
import ch.resrc.testing.capabilities.json.Json;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.Random;
import java.util.stream.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class SortResource {

    private final Random numberGenerator;
    private final Json json;

    SortResource(Random numberGenerator, Json json) {
        this.numberGenerator = numberGenerator;
        this.json = json;
    }

    HttpProtocolBuilder httpProtocol() {
        return http
                .baseUrl("http://localhost:8080/api/sort")
                .acceptHeader("application/json; charset=UTF-8")
                .acceptEncodingHeader("gzip, deflate")
                .acceptLanguageHeader("en-US,en;q=0.5")
                .contentTypeHeader("application/json; charset=UTF-8")
                .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:95.0) Gecko/20100101 Firefox/95.0");
    }

    ChainBuilder request(String endpoint, int randomNumbersCount) {
        return exec(
                http("%d-randomNumbers".formatted(randomNumbersCount))
                        .post(endpoint)
                        .body(StringBody(
                                        json.toJsonString(
                                                new UnsortedListDto(
                                                        randomNumbersInRange(1, randomNumbersCount)
                                                                .toList()
                                                )
                                        )
                                )
                        )
        );
    }

    private Stream<Integer> randomNumbersInRange(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive)
                .mapToObj(numberGenerator::nextInt);
    }
}
