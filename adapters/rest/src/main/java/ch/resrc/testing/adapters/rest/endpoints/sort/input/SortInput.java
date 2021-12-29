package ch.resrc.testing.adapters.rest.endpoints.sort.input;

import ch.resrc.testing.adapters.rest.input.ClientInput;
import ch.resrc.testing.adapters.rest.endpoints.sort.dto.UnsortedListDto;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.json.JsonBody;
import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.capabilities.validation.*;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;

import java.util.List;

public class SortInput implements Validatable<SortInput>, ClientInput {

    private final Client client;
    private final JsonBody<UnsortedListDto> body;

    public SortInput(Client client, JsonBody<UnsortedListDto> body) {
        this.client = client;
        this.body = body;
    }

    @Override
    public Client client() {
        return client;
    }

    @Override
    public Result<SortInput, ValidationError> validated() {
        return validatedInput().thenValueOf(() -> this);
    }

    public Sort.Input<Integer> input() {
        return new Sort.Input<>(this.client, unsortedList());
    }

    private List<Integer> unsortedList() throws InvalidInputDetected {
        return validatedInput().getOrThrow(InvalidInputDetected::of);
    }

    private Result<List<Integer>, ValidationError> validatedInput() {
        return body
                .parsingResult(UnsortedListDto.class)
                .flatMap(this::toUnsortedList);
    }

    private Result<List<Integer>, ValidationError> toUnsortedList(UnsortedListDto dto) {
        return Result.success(dto.unsorted);
    }
}
