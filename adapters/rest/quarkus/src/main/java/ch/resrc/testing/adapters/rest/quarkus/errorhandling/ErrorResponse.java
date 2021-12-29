package ch.resrc.testing.adapters.rest.quarkus.errorhandling;

import ch.resrc.testing.adapters.rest.errorhandling.*;
import ch.resrc.testing.adapters.rest.output.HavingPresentation;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.capabilities.error_handling.faults.OurFault;
import ch.resrc.testing.capabilities.json.*;
import ch.resrc.testing.capabilities.presentation.ErrorPresenter;

import javax.ws.rs.core.Response;
import java.time.*;
import java.util.List;

import static ch.resrc.testing.adapters.rest.errorhandling.RestProblem.*;
import static ch.resrc.testing.capabilities.functional.PersistentCollections.addedTo;
import static ch.resrc.testing.capabilities.functional.Predicates.satisfies;
import static io.vavr.API.*;
import static org.apache.commons.lang3.StringUtils.leftPad;

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
    public void presentSystemFailure(Client client, RuntimeException failure) {
        this.reportedErrors = this.reportedErrors.systemFailureReported(failure);
    }

    @Override
    public void presentBusinessError(Client client, BusinessError businessError) {
        this.reportedErrors = this.reportedErrors.businessErrorReported(businessError);
    }

    public Response asResponseEntity() { return reportedErrors.asResponseEntity(); }

    @Override
    public boolean isPresentationMissing() { return false; }

    static class HttpError {
        Response.Status statusCode;
        ErrorDto.Error errorBody;
    }

    private HttpError httpError(Integer httpStatus, ProblemDetected fault) {
        var problemCode = problemCatalogue.codeFor(fault.problem());
        var type = "RESRC-TESTING-" + (leftPad(problemCode.toString(), 4, "0"));
        var errorDto = new ErrorDto.Error();

        errorDto.setType(type)
                .setTitle(fault.title())
                .setDetails(fault.details())
                .setInstance(fault.id().toString())
                .setOccurredOn(OffsetDateTime.ofInstant(fault.occurredOn(), ZoneId.systemDefault()).toString());

        return new HttpError() {{
            statusCode = Response.Status.fromStatusCode(httpStatus);
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

        Response asResponseEntity();

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
        public Response asResponseEntity() {
            HttpError reportedError = errors.get(0);
            return Response
                    .status(reportedError.statusCode)
                    .entity(asJsonBody(List.of(reportedError)))
                    .build();
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
        public Response asResponseEntity() {
            JsonBody<?> body = asJsonBody(errors);
            return Response.status(statusCode()).entity(body).build();
        }

        private Response.Status statusCode() {
            List<Response.Status> distinctStatusCodes = errors.stream()
                    .map(x -> x.statusCode)
                    .distinct()
                    .toList();

            if (distinctStatusCodes.size() > 1) {
                return Response.Status.BAD_REQUEST;
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
        public Response asResponseEntity() {
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .entity(JsonBody.errorBodyOf("", json))
                    .build();
        }
    }
}
