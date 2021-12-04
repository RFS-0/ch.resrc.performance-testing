package ch.resrc.testing.use_cases.sort.ports.documents;

import java.util.List;

public class SortedDocument<T> {

    private final List<T> sortedList;

    public SortedDocument(List<T> sortedList) {
        this.sortedList = sortedList;
    }

    public List<T> sortedList() {
        return sortedList;
    }
}
