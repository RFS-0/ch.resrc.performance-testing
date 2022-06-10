package ch.resrc.testing.test_capabilities.assertions;

import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.capabilities.result.Result;
import org.assertj.core.api.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class ResultAssert<T, E> extends AbstractAssert<ResultAssert<T, E>, Result<T, E>> {

    private ResultAssert(Result<T, E> actual) {

        super(actual, ResultAssert.class);
    }

    public static <U, E> ResultAssert<U, E> assertThatResult(Result<U, E> actual) {

        return new ResultAssert<>(actual);
    }

    public static <U, E> ObjectAssert<E> assertThatResultError(Function<List<E>, E> selector, Result<U, E> actual) {

        return assertThatResult(actual).andError(selector);
    }

    public ResultAssert<T, E> isSuccess() {

        if (!actual.isSuccess()) {
            String errors = actual.errors().stream().map(Objects::toString).collect(Collectors.joining("\n"));
            failWithMessage("Expected success but was failure: \n%s", errors);
        }

        return this;
    }

    public ResultAssert<T, E> isFailure() {

        if (!actual.isFailure()) {
            failWithMessage("Expected result to be a failure but wasn't");
        }

        return this;
    }

    public ResultAssert<T, E> hasValueSatisfying(Consumer<T> requirements) {

        isSuccess();
        return satisfies(actual -> requirements.accept(actual.value()));
    }


    public ResultAssert<T, E> hasDiagnosedProblem(Problem expected) {

        return hasDiagnosedProblem(expected, x -> {});
    }

    public ResultAssert<T, E> hasDiagnosedProblem(Problem expected, Consumer<? super ProblemDiagnosis> requirements) {

        isFailure();

        List<ProblemDiagnosis> problems = actual.errors().stream()
                .map(HavingDiagnosis.class::cast)
                .map(HavingDiagnosis::diagnosis).toList();

        List<ProblemDiagnosis> candidates = problems.stream()
                                                    .filter(candidate -> candidate.problem().equals(expected))
                                                    .collect(toList());

        String actualErrorsForCiting = actual.errors().stream()
                                             .map(E::toString)
                                             .collect(Collectors.joining("\n"));


        if (candidates.isEmpty()) {
            failWithMessage("Problem <%s> was not reported. Actual:\n%s", expected, actualErrorsForCiting);
        }

        assertThat(candidates).anySatisfy(requirements);

        return this;
    }

    public ResultAssert<T,E> hasOneError() {

        if (actual.isSuccess()) {
            failWithMessage("Result is a success there are no errors.");
        } else if (actual.errors().size() > 1) {
            failWithMessage("Result has <%s> errors, not one.", actual.errors().size());
        }

        return this;
    }

    public ObjectAssert<E> andError(Function<List<E>, E> selector) {

        E actualError = selector.apply(actual.errors());

        return assertThat(actualError);
    }
}
