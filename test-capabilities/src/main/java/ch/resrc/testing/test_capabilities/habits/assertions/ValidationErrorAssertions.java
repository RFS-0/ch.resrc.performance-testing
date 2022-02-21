package ch.resrc.testing.test_capabilities.habits.assertions;

import ch.resrc.testing.capabilities.validation.ValidationError;
import org.hamcrest.Matcher;

import java.util.List;

import static ch.resrc.testing.capabilities.validation.ValidationError.UNKNOWN_ORIGIN;
import static ch.resrc.testing.test_capabilities.habits.assertions.CustomMatchers.whereAttribute;
import static org.hamcrest.Matchers.*;

public class ValidationErrorAssertions {

    public static Matcher<Iterable<? extends ValidationError>> containsAnErrorMessageWithText(String errorMessageText) {
        return contains(errorMessage(containsString(errorMessageText)));
    }

    public static Matcher<ValidationError> errorMessage(Matcher<String> messageMatcher) {
        return whereAttribute(ValidationError::errorMessage, "error message", messageMatcher);
    }

    public static Matcher<ValidationError> hasNoOrigin() {
        return whereAttribute(ValidationError::origin, "origin", is(UNKNOWN_ORIGIN));
    }
}
