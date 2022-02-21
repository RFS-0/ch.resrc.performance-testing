package ch.resrc.testing.test_capabilities.habits.assertions;

import ch.resrc.testing.capabilities.events.Events;
import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.test_capabilities.assertions.*;
import org.assertj.core.api.Assertions;
import org.hamcrest.*;

import static ch.resrc.testing.test_capabilities.assertions.EventsAssert.assertThatEvents;

public class AssertionHabits extends Assertions {

    public static <T> void assertThat(T actual, Matcher<? super T> matcher) {

        MatcherAssert.assertThat(actual, matcher);
    }

    public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {

        MatcherAssert.assertThat(reason, actual, matcher);
    }

    public static <T, E> ResultAssert<T, E> assertThat(Result<T, E> actualResult) {

        return ResultAssert.assertThatResult(actualResult);
    }

    public static EventsAssert assertThat(Events events) {

        return assertThatEvents(events);
    }
}
