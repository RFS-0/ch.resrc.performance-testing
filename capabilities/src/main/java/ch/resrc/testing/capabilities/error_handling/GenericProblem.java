package ch.resrc.testing.capabilities.error_handling;

public enum GenericProblem implements Problem {

    BAD_REQUEST("Bad request", "Your request fails to validate or violates preconditions."),

    SYSTEM_FAILURE("Internal system problem", "The system is currently unable to serve your request."),

    COMMUNICATION_FAILURE("Communication failure", "The System is currently unable to communicate to some of its partner systems.");

    private final String title;
    private final String details;

    GenericProblem(String title, String details) {
        this.title = title;
        this.details = details;
    }

    @Override
    public String detailsTemplate() {
        return details;
    }

    @Override
    public String title()
    {
        return title;
    }
}
