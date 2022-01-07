package ch.resrc.testing.adapters.rest.spring_boot.errorhandling;

import ch.resrc.testing.adapters.rest.errorhandling.*;
import ch.resrc.testing.adapters.rest.output.HavingPresentation;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.capabilities.error_handling.faults.OurFault;
import ch.resrc.testing.capabilities.json.*;
import ch.resrc.testing.capabilities.presentation.ErrorPresenter;
import org.springframework.http.*;

import java.time.*;
import java.util.List;

import static ch.resrc.testing.adapters.rest.errorhandling.RestProblem.*;
import static ch.resrc.testing.capabilities.functional.PersistentCollections.addedTo;
import static ch.resrc.testing.capabilities.functional.Predicates.satisfies;
import static io.vavr.API.*;
import static org.apache.commons.lang3.StringUtils.leftPad;

/**
 * Renders the HTTP error responses to be returned by all REST resources if an error occurs.
 *
 * @implNote Uses the state machine pattern to encode the behaviour that depends on the kind of errors
 * that are reported.
 */
public class ErrorResponse implements ErrorPresenter, HavingPresentation {

    private final ProblemCatalogue problemCatalogue;
    private final Json json;

    private ReportedErrors reportedErrors = new NoErrors();

    public ErrorResponse(ProblemCatalogue problemCatalogue, Json json) {

        this.problemCatalogue = problemCatalogue;
        this.json = json;
    }

    public boolean hasErrors() { return this.reportedErrors.hasErrors(); }

    @Override
    public void presentSystemFailure(Client client, RuntimeException bad) {
        reportedErrors = reportedErrors.systemFailureReported(bad);
    }

    @Override
    public void presentBusinessError(Client client, BusinessError businessError) {

        reportedErrors = reportedErrors.businessErrorReported(businessError);
    }

    public <T> ResponseEntity<JsonBody<T>> asResponseEntity() { return reportedErrors.asResponseEntity(); }


    @Override
    public boolean isPresentationMissing() { return false; }


    static class HttpError {

        HttpStatus statusCode;
        ErrorDto.Error errorBody;
    }

    private HttpError httpError(Integer httpStatus, ProblemDetected fault) {


        var problemCode = problemCatalogue.codeFor(fault.problem());
        var type = "TESTING-" + (leftPad(problemCode.toString(), 4, "0"));

        var errorDto = new ErrorDto.Error();
        errorDto.setType(type)
                .setTitle(fault.title())
                .setDetails(fault.details())
                .setInstance(fault.id().toString())
                .setOccurredOn(OffsetDateTime.ofInstant(fault.occurredOn(), ZoneId.systemDefault()).toString());

        return new HttpError() {{
            statusCode = HttpStatus.valueOf(httpStatus);
            errorBody = errorDto;
        }};
    }

    private <T> JsonBody<T> asJsonBody(List<HttpError> errors) {

        var errorDto = new ErrorDto();
        errors.forEach(x -> errorDto.addError(x.errorBody));

        return JsonBody.errorBodyOf(errorDto, json);
    }

    ///// State machine /////

    private interface ReportedErrors {

        ReportedErrors systemFailureReported(RuntimeException failure);

        ReportedErrors businessErrorReported(BusinessError businessError);

        <T> ResponseEntity<JsonBody<T>> asResponseEntity();

        boolean hasErrors();
    }

    private class SystemFailures implements ReportedErrors {

        private List<HttpError> errors = List.of();

        SystemFailures(RuntimeException failure) { this.systemFailureReported(failure); }

        @Override
        public ReportedErrors systemFailureReported(RuntimeException failure) {

            HttpError error = httpError(500, OurFault.of(failure));

            this.errors = addedTo(this.errors, error);

            return this;
        }

        @Override
        public ReportedErrors businessErrorReported(BusinessError businessError) {
            // Ignored. System failures override any business errors.
            // Business errors are not presented if system failures have occurred.
            return this;
        }

        @Override
        public boolean hasErrors() { return !errors.isEmpty(); }

        @Override
        public <T> ResponseEntity<JsonBody<T>> asResponseEntity() {

            HttpError reportedError = errors.get(0);

            return ResponseEntity.status(reportedError.statusCode)
                                 .body(asJsonBody(List.of(reportedError)));
        }
    }

    private class BusinessErrors implements ReportedErrors {

        private List<HttpError> errors = List.of();

        BusinessErrors(BusinessError businessError) { this.businessErrorReported(businessError); }

        @Override
        public ReportedErrors systemFailureReported(RuntimeException failure) {

            return new SystemFailures(failure);
        }

        @Override
        public ReportedErrors businessErrorReported(BusinessError businessError) {

            this.errors = addedTo(this.errors, this.clientFaultResponse(businessError));

            return this;
        }

        private HttpError clientFaultResponse(BusinessError businessError) {

            ProblemDetected fault = businessError.asException();

            return Match(fault.problem()).of(
                    Case($(satisfies(RestProblem.class, problem -> problem.is(RESOURCE_NOT_FOUND))),
                         httpError(404, fault)
                    ),
                    Case($(satisfies(RestProblem.class, problem -> problem.is(HTTP_METHOD_NOT_SUPPORTED))),
                         httpError(400, fault)
                    ),
                    Case($(), httpError(400, fault))
            );
        }

        @Override
        public boolean hasErrors() { return !errors.isEmpty(); }

        @Override
        public <T> ResponseEntity<JsonBody<T>> asResponseEntity() {

            JsonBody<T> body = asJsonBody(errors);
            return ResponseEntity.status(statusCode()).body(body);
        }

        private HttpStatus statusCode() {

            List<HttpStatus> distinctStatusCodes = errors.stream()
                    .map(x -> x.statusCode)
                    .distinct()
                    .toList();

            if (distinctStatusCodes.size() > 1) {
                return HttpStatus.BAD_REQUEST;
            } else {
                return distinctStatusCodes.get(0);
            }
        }
    }

    private class NoErrors implements ReportedErrors {

        @Override
        public ReportedErrors systemFailureReported(RuntimeException failure) {

            return new SystemFailures(failure);
        }

        @Override
        public ReportedErrors businessErrorReported(BusinessError businessError) {

            return new BusinessErrors(businessError);
        }

        @Override
        public boolean hasErrors() { return false; }

        @Override
        public <T> ResponseEntity<JsonBody<T>> asResponseEntity() {

            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(JsonBody.errorBodyOf("", json));
        }
    }

}
