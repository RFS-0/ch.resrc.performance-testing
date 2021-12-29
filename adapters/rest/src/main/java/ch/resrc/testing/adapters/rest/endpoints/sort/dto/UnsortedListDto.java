package ch.resrc.testing.adapters.rest.endpoints.sort.dto;

import java.util.List;

public class UnsortedListDto {

    public List<Integer> unsorted = List.of();

    public UnsortedListDto() {
        // for JSON deserialization
    }
}
