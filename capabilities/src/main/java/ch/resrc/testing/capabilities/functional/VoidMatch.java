package ch.resrc.testing.capabilities.functional;

import io.vavr.API;

import java.util.function.*;

import static io.vavr.API.$;

public class VoidMatch {

    public static <T> API.Match.Case<T, Void> Case(API.Match.Pattern0<T> pattern, Consumer<T> f) {
        return API.Case(pattern, (x) -> {
            f.accept(x);
            return null;
        });
    }

    public static <T, U, R> API.Match.Case<T, Void> Case(API.Match.Pattern2<T, T, U> pattern, BiFunction<T, U, R> f) {
        return API.Case(pattern, (T x, U y) -> {
                    f.apply(x, y);
                    return null;
                }
        );
    }

    public static <T> API.Match.Case<T, Void> DefaultIgnore() {
        return API.Case($(), (x) -> null);
    }
}
