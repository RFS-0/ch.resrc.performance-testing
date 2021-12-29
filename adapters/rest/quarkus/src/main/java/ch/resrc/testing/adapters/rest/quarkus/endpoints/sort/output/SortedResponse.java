package ch.resrc.testing.adapters.rest.quarkus.endpoints.sort.output;

import ch.resrc.testing.adapters.rest.endpoints.sort.dto.SortedListDto;
import ch.resrc.testing.adapters.rest.quarkus.output.RestResponse;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.ProblemCatalogue;
import ch.resrc.testing.capabilities.json.Json;
import ch.resrc.testing.use_cases.sort.ports.documents.SortedDocument;
import ch.resrc.testing.use_cases.sort.ports.outbound.SortPresenter;

import javax.ws.rs.core.Response;
import java.util.List;

public class SortedResponse extends RestResponse<List<SortedListDto>> implements SortPresenter {

    private Response documentPresentation;

    public SortedResponse(int successHttpStatus,
                          ProblemCatalogue problemCatalogue,
                          Json json) {
        super(successHttpStatus, problemCatalogue, json);
    }

    @Override
    public <T extends Comparable<T>> void present(Client client, SortedDocument<T> toBePresented) {
        SortedListDto<T> sorted = SortedListDto.fromDocument(toBePresented);
        documentPresentation = Response
                .status(successHttpStatus)
                .header("clientId", client.id())
                .entity(sorted)
                .build();
    }

    @Override
    protected Response documentPresentation() {
        return documentPresentation;
    }

    @Override
    protected boolean hasDocumentPresentation() {
        return documentPresentation != null;
    }
}
