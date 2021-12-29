package ch.resrc.testing.capabilities.functional;

import java.util.function.*;

import static java.util.Objects.requireNonNull;

public class Predicates {

    public static <T> Predicate<T> satisfies(Class<? extends T> type, Predicate<T> condition) {

        requireNonNull(type, "Type must not be null");
        requireNonNull(condition, "Condition must not be null");

        return (T object) -> {
            if (object != null && !type.isAssignableFrom(object.getClass())) return false;

            return condition.test(type.cast(object));
        };
    }

    public static <T> Predicate<T> pred(Function<T, Boolean> f) { return f::apply; }
}
