package ch.resrc.testing.adapters.rest.endpoints.sort.dto;

import ch.resrc.testing.use_cases.sort.ports.documents.SortedDocument;

import java.util.List;

public class SortedListDto<T> {

    public List<T> sorted = List.of();

    public SortedListDto() {
        // for JSON deserialization
    }

    public static <T> SortedListDto<T> fromDocument(SortedDocument<T> document) {
        SortedListDto<T> dto = new SortedListDto<>();
        dto.sorted = document.sortedList();
        return dto;
    }
}
