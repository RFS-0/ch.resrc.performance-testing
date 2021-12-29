package app.integration_tests.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class SortResourceTest {

    @Test
    void validInput_basicBubbleSort_success() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "unsorted": [5,4,3,2,1]
                        }
                        """)
                .when()
                .post("/sort/basic-bubble-sort")
                .then()
                .assertThat()
                .statusCode(201)
                .body("sorted", equalTo(List.of(1, 2, 3, 4, 5)));
    }

    @Test
    void validInput_optimizedBubbleSort_success() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "unsorted": [5,4,3,2,1]
                        }
                        """)
                .when()
                .post("/sort/optimized-bubble-sort")
                .then()
                .assertThat()
                .statusCode(201)
                .body("sorted", equalTo(List.of(1, 2, 3, 4, 5)));
    }

    @Test
    void validInput_redBlackTree_success() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "unsorted": [5,4,3,2,1]
                        }
                        """)
                .when()
                .post("/sort/red-black-tree")
                .then()
                .assertThat()
                .statusCode(201)
                .body("sorted", equalTo(List.of(1, 2, 3, 4, 5)));
    }
}