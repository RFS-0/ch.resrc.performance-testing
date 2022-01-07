package ch.resrc.testing.adapters.rest.spring_boot.endpoints.sort.output;

import ch.resrc.testing.adapters.rest.endpoints.sort.dto.SortedListDto;
import ch.resrc.testing.adapters.rest.spring_boot.output.RestResponse;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.ProblemCatalogue;
import ch.resrc.testing.capabilities.json.*;
import ch.resrc.testing.use_cases.sort.ports.documents.SortedDocument;
import ch.resrc.testing.use_cases.sort.ports.outbound.SortPresenter;
import org.springframework.http.ResponseEntity;

public class SortedResponse extends RestResponse<SortedListDto<? extends Comparable<?>>> implements SortPresenter {

    private ResponseEntity<JsonBody<SortedListDto<? extends Comparable<?>>>> documentPresentation;

    public SortedResponse(int successHttpStatus, ProblemCatalogue problemCatalogue, Json json) {
        super(successHttpStatus, problemCatalogue, json);
    }

    @Override
    public <T extends Comparable<T>> void present(Client client, SortedDocument<T> toBePresented) {
        SortedListDto<T> sorted = SortedListDto.fromDocument(toBePresented);

        documentPresentation = ResponseEntity
                .status(successHttpStatus)
                .body(JsonBody.responseBodyOf(sorted, json));
    }

    @Override
    protected ResponseEntity<JsonBody<SortedListDto<? extends Comparable<?>>>> documentPresentation() {
        return documentPresentation;
    }

    @Override
    protected boolean hasDocumentPresentation() {
        return documentPresentation != null;
    }
}
