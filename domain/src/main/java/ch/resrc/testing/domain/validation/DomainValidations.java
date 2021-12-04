package ch.resrc.testing.domain.validation;


import ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis;
import ch.resrc.testing.capabilities.validation.ValidationError;
import ch.resrc.testing.domain.error_handling.*;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class DomainValidations {

    public static Function<ValidationError, ProblemDiagnosis> mandatoryPropertyMissing() {
        return (ValidationError error) -> ProblemDiagnosis.of(DomainProblem.MANDATORY_VALUE_MISSING)
                .withContext("message", error.errorMessage());
    }

    public static Function<List<ValidationError>, DomainProblemDetected> invariantViolated() {
        return (List<ValidationError> errors) -> {
            var problems = errors.stream()
                    .map(error -> ProblemDiagnosis.of(DomainProblem.INVARIANT_VIOLATED)
                            .withContext("message", error.errorMessage())
                    )
                    .collect(toList());

            return DomainProblemDetected.of(problems);
        };
    }
}
