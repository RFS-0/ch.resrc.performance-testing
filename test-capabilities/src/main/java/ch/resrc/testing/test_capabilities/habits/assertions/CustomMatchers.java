package ch.resrc.testing.test_capabilities.habits.assertions;

import org.assertj.core.api.Assertions;
import org.assertj.core.matcher.AssertionMatcher;
import org.assertj.core.util.Streams;
import org.hamcrest.*;

import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.not;

public class CustomMatchers {

    public static <T> AssertionMatcher<T> satisfies(Class<T> expectedType, Consumer<T> requirements) {

        return new AssertionMatcher<>() {

            @Override
            public void assertion(T actual) throws AssertionError {

                Assertions.assertThat(actual).isInstanceOfSatisfying(expectedType, requirements);
            }
        };
    }

    public static <T, U> Matcher<T> whereAttribute(Function<T, U> attributeLense,
                                                   String attr,
                                                   Matcher<U> attributeMatcher) {

        return new FeatureMatcher<>(attributeMatcher, attr + " matches", attr) {
            @Override
            protected U featureValueOf(T actual) {

                return attributeLense.apply(actual);
            }
        };
    }

    public static <T, U> Matcher<T> whereAttribute(Function<T, U> attributeLense, Matcher<U> attributeMatcher) {

        return whereAttribute(attributeLense, "", attributeMatcher);
    }

    public static <T, U> Matcher<T> instanceOf(Class<U> type, Matcher<? super U> instanceMatcher) {

        var instanceOf = Matchers.instanceOf(type);

        return new TypeSafeDiagnosingMatcher<>() {

            @Override
            protected boolean matchesSafely(T item, Description mismatchDescription) {

                boolean isInstance = instanceOf.matches(item);
                if (!isInstance) instanceOf.describeMismatch(item, mismatchDescription);

                boolean isInstanceMatching = false;
                if (isInstance) {
                    isInstanceMatching = instanceMatcher.matches(type.cast(item));
                    if (!isInstanceMatching) instanceMatcher.describeMismatch(type.cast(item), mismatchDescription);
                }

                return isInstance && isInstanceMatching;
            }

            @Override
            public void describeTo(Description description) {

                instanceOf.describeTo(description);
                description.appendText(" where ");
                instanceMatcher.describeTo(description);
            }
        };
    }

    public static List<SelfDescribing> asSelfDescribing(Iterable<?> items) {

        return Streams.stream(items)
                .map(x -> (SelfDescribing) (descr -> descr.appendText(x.toString())))
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> Matcher<Iterable<? extends T>> containsExactly(Matcher<? super T>... matchers) {

        return Matchers.contains(matchers);
    }

    @SafeVarargs
    public static <T> Matcher<Iterable<? extends T>> containsExactlyInAnyOrder(Matcher<? super T>... matchers) {

        return Matchers.containsInAnyOrder(matchers);
    }

    public static <T> Matcher<Iterable<? extends T>> hasNone(Matcher<? super T> unexpected) {

        return not(hasOneOrMore(unexpected));
    }

    public static <T> Matcher<Iterable<? extends T>> hasOneOrMore(Matcher<? super T> expected) {

        var itemMatcher = Matchers.<T>hasItem(expected);

        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(Iterable<? extends T> elements, Description mismatchDescription) {

                itemMatcher.describeMismatch(elements, mismatchDescription);
                return itemMatcher.matches(elements);
            }

            @Override
            public void describeTo(Description description) {

                itemMatcher.describeTo(description);
            }
        };
    }

    public static <T> Matcher<Iterable<? extends T>> hasOne(Matcher<? super T> expected) {

        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(Iterable<? extends T> elements, Description mismatchDescription) {

                long numMatching = Streams.stream(elements)
                        .filter(expected::matches)
                        .count();

                mismatchDescription
                        .appendValue(numMatching)
                        .appendText(" elements(s) matching (")
                        .appendDescriptionOf(expected)
                        .appendText(") are contained in collection with elements:\n")
                        .appendList("[", "\n", "]", asSelfDescribing(elements));

                return numMatching == 1L;
            }

            @Override
            public void describeTo(Description description) {

                description.appendText("contains exactly one element matching ")
                        .appendDescriptionOf(expected);
            }
        };
    }
}
