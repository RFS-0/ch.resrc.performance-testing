package ch.resrc.testing.use_cases.sort.ports.events;

import ch.resrc.testing.capabilities.events.Event;
import ch.resrc.testing.use_cases.sort.ports.documents.SortedDocument;

import java.util.List;

public class Sorted<T> extends Event {

    private final SortedDocument<T> document;

    private Sorted(SortedDocument<T> document) {
        this.document = document;
    }

    public static <T> Sorted<T> of(List<T> sortedList) {
        return new Sorted<>(new SortedDocument<>(sortedList));
    }

    public SortedDocument<T> document() {
        return document;
    }
}
