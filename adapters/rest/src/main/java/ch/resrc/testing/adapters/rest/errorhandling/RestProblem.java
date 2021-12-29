package ch.resrc.testing.adapters.rest.errorhandling;

import ch.resrc.testing.capabilities.error_handling.Problem;

public enum RestProblem implements Problem {

    HTTP_METHOD_NOT_SUPPORTED("Unsupported HTTP method", "We support [${supportedMethods}] for this resource but not ${unsupportedHttpMethod}."),

    RESOURCE_NOT_FOUND("Resource not found", "Cannot serve ${httpMethod} ${path}."),

    USE_CASE_RESULT_MISSING("Use case result missing.", "Use case failed to present any result.");

    private final String title;
    private final String detailsTemplate;


    RestProblem(String title, String detailsTemplate)
    {
        this.title = title;
        this.detailsTemplate = detailsTemplate;
    }

    @Override
    public String title()
    {
        return title;
    }

    @Override
    public String detailsTemplate()
    {
        return detailsTemplate;
    }
}
