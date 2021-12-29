package ch.resrc.testing.products.quarkus.app_config.problem;


import ch.resrc.testing.adapters.rest.errorhandling.RestProblem;
import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.domain.error_handling.DomainProblem;
import ch.resrc.testing.use_cases.support.habits.errorhandling.UseCaseProblem;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

import static ch.resrc.testing.capabilities.error_handling.ProblemCode.code;
import static java.util.Map.entry;

@ApplicationScoped
public class TestingProblemCatalogue implements ProblemCatalogue {

    static final List<Map.Entry<Problem, ProblemCode>> codeCatalogue = List.of(
            entry(GenericProblem.SYSTEM_FAILURE, code(9001)),
            entry(GenericProblem.BAD_REQUEST, code(9002)),
            entry(GenericProblem.COMMUNICATION_FAILURE, code(9003)),

            entry(DomainProblem.INVALID_PROPERTY_MUTATION, code(2000)),
            entry(DomainProblem.MANDATORY_VALUE_MISSING, code(2001)),
            entry(DomainProblem.INVARIANT_VIOLATED, code(2002)),

            entry(UseCaseProblem.INVALID_INPUT_DETECTED, code(3001)),

            entry(RestProblem.HTTP_METHOD_NOT_SUPPORTED, code(4001)),
            entry(RestProblem.RESOURCE_NOT_FOUND, code(4002)),
            entry(RestProblem.USE_CASE_RESULT_MISSING, code(4003))
    );

    public ProblemCode codeFor(Problem problem)
    {
        return codeCatalogue.stream()
                .dropWhile(entry -> !entry.getKey().equals(problem))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(ProblemCode.UNDEFINED);
    }

    List<Map.Entry<Problem, ProblemCode>> entries() {
        return codeCatalogue;
    }


}
