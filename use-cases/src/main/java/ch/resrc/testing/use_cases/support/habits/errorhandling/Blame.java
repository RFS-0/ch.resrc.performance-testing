package ch.resrc.testing.use_cases.support.habits.errorhandling;


import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.capabilities.error_handling.faults.*;
import ch.resrc.testing.capabilities.result.*;
import ch.resrc.testing.capabilities.validation.*;

import java.util.*;
import java.util.function.*;

import static ch.resrc.testing.use_cases.support.habits.errorhandling.UseCaseProblem.INVALID_INPUT_DETECTED;
import static io.vavr.Predicates.instanceOf;
import static java.util.stream.Collectors.toList;

public class Blame {

    /**
     * Returns a predicate that tells whether a given event signals that
     * a {@link ClientFault} has occurred. Useful for matching error events and invoking an
     * appropriate error reaction.
     *
     * @return the predicate, as explained
     */
    public static Predicate<ClientFault> isClientFault() {

        return instanceOf(ClientFault.class);
    }

    public static <T, E> Function<Optional<T>, Result<T, E>> orElseFailure(Supplier<E> problem) {

        return (Optional<T> optional) -> optional.map(Result::<T, E>success)
                .orElse(Result.failure(problem.get()));
    }

    public static <X extends RuntimeException, Y extends X> Consumer<X> throwUnless(Class<Y> noThrow) throws X {

        return (X error) -> {

            if (noThrow.isAssignableFrom(error.getClass())) {
                return;
            }

            throw error;
        };
    }

    /**
     * Returns an {@link ErrorHandling} that maps the given problems to {@link ClientFault}s.
     * The transform throws an {@link OurFault} for the first problem that is not contained in the list of expected
     * problems because unexpected problems signal a system malfunction.
     *
     * @param expectedProblems the problems that should be interpreted as {@code ClientFault}s
     * @param <T>              the value type of the result that gets transformed
     * @param <E>              the error type of the result that gets transformed
     *
     * @return an {@link ErrorHandling}, as explained
     */
    public static <T, E extends HavingDiagnosis> ErrorHandling<T, E, ClientFault> expectAsClientFaults(
            Problem... expectedProblems) {

        return (List<E> errors) -> {

            List<ClientFault> clientFaults = errors
                    .stream()
                    .map(blameClientFor(expectedProblems))
                    .peek(throwUnless(ClientFault.class))
                    .map(ClientFault.class::cast)
                    .collect(toList());

            return Result.failure(clientFaults);
        };
    }

    /**
     * Returns an {@link ErrorHandling} that throws an exception if the result to be handled is a Failure.
     * In that case the exception representation of the first error is thrown .
     * <p>
     * This error handling represents our error handling pattern if an operation should not produce any errors unless
     * the system malfunctions.
     * </p>
     *
     * @param <T> the value type of the result that gets handled
     * @param <E> the error type of the result that gets handled
     *
     * @return an error handling, as explained.
     */
    public static <T, E extends HavingExceptionRepresentation> ErrorHandling<T, E, E> expectNoProblems() {

        return (List<E> errors) -> {
            throw errors.get(0).asException();
        };
    }

    public static ClientFault asClientFault(ValidationError validationError) {

        return ClientFault.of(
                ProblemDiagnosis
                        .of(INVALID_INPUT_DETECTED)
                        .withContext("validationMessage", validationError.errorMessage())
        );
    }

    public static ClientFault asClientFault(InvalidInputDetected bad) {

        return ClientFault.of(
                ProblemDiagnosis
                        .of(INVALID_INPUT_DETECTED)
                        .withContext("validationMessage", bad.getMessage())
                        .withCause(bad));
    }

    public static <E extends HavingDiagnosis> Function<E, Fault> blameClientFor(Problem... blamableProblems) {

        return (E error) -> {

            Problem diagnosedProblem = error.diagnosis().problem();

            if (List.of(blamableProblems).contains(diagnosedProblem)) {
                return ClientFault.of(error.diagnosis());
            } else {
                return OurFault.of(error.diagnosis());
            }
        };
    }
}
