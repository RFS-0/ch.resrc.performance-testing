package ch.resrc.testing.test_capabilities.habits.use_cases;

import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;

import java.util.List;
import java.util.function.Consumer;

public class SortInputSpec<T extends Comparable<T>> {

    private Client client;
    private List<T> toBeSorted;

    private SortInputSpec() {
    }

    public SortInputSpec(SortInputSpec<T> other) {
        this.client = other.client;
        this.toBeSorted = other.toBeSorted;
    }

    private SortInputSpec<T> copied(Consumer<SortInputSpec<T>> modification) {
        var copy = new SortInputSpec<>(this);
        modification.accept(copy);
        return copy;
    }


    public static <T extends Comparable<T>> SortInputSpec<T> createSortInput() {
        return new SortInputSpec<>();
    }

    public SortInputSpec<T> client(Client client) {
        return copied(but -> but.client = client);
    }

    public SortInputSpec<T> toBeSorted(List<T> toBeSorted) {
        return copied(but -> but.toBeSorted = toBeSorted);
    }

    public Sort.Input<T> asInput() {
        return new Sort.Input<>(client, toBeSorted);
    }
}
