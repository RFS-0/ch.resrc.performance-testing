package ch.resrc.testing.capabilities.functional;

import java.util.List;
import java.util.function.Consumer;

public class ForEach {

    public static <T, U> Consumer<List<T>> forEach(Consumer<T> effect) {

        return (List<T> list) -> list.forEach(effect);
    }
}
