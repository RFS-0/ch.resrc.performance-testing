package ch.resrc.testing.adapters.rest.spring_boot.output;

import ch.resrc.testing.adapters.rest.spring_boot.errorhandling.ErrorResponse;
import ch.resrc.testing.adapters.rest.output.HavingPresentation;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.capabilities.json.*;
import ch.resrc.testing.capabilities.presentation.ErrorPresenter;
import org.springframework.http.ResponseEntity;

/**
 * Base class for all REST responses that act as presenters to use cases.
 * Implements the common error presentation logic. Concrete subclasses take
 * care of the happy case presentation of the response body.
 *
 * @param <T> the DTO type that defines the body of the response.
 *
 * @implNote template method pattern
 */
public abstract class RestResponse<T> implements ErrorPresenter, HavingPresentation, ProvideJsonBodyResponseEntity<T> {

    protected final int successHttpStatus;
    protected final Json json;
    protected final ErrorResponse errorResponse;

    protected RestResponse(int successHttpStatus, ProblemCatalogue problemCatalogue, Json json) {

        this.successHttpStatus = successHttpStatus;
        this.errorResponse = new ErrorResponse(problemCatalogue, json);
        this.json = json;
    }

    protected abstract ResponseEntity<JsonBody<T>> documentPresentation();

    protected abstract boolean hasDocumentPresentation();

    @Override
    public ResponseEntity<JsonBody<T>> asResponseEntity() {

        if (errorResponse.hasErrors()) {
            return errorResponse.asResponseEntity();
        } else {
            return documentPresentation();
        }
    }

    @Override
    public void presentSystemFailure(Client client, RuntimeException failure) {

        this.errorResponse.presentSystemFailure(client, failure);
    }

    @Override
    public void presentBusinessError(Client client, BusinessError businessError) {

        this.errorResponse.presentBusinessError(client, businessError);
    }

    @Override
    public boolean isPresentationMissing() {

        return !hasDocumentPresentation() && !this.errorResponse.hasErrors();
    }

}
