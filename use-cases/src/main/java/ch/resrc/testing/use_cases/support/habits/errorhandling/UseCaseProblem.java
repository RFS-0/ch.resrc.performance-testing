package ch.resrc.testing.use_cases.support.habits.errorhandling;

import ch.resrc.testing.capabilities.error_handling.Problem;

public enum UseCaseProblem implements Problem {

    INVALID_INPUT_DETECTED("Invalid input", "${validationMessage}");

    private final String title;
    private final String detailsTemplate;

    UseCaseProblem(String title, String detailsTemplate) {
        this.title = title;
        this.detailsTemplate = detailsTemplate;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String detailsTemplate() {
        return detailsTemplate;
    }
}
