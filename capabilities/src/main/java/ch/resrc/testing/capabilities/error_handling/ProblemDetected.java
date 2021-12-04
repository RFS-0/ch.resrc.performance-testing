package ch.resrc.testing.capabilities.error_handling;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis.aProblemDiagnosis;
import static java.util.Objects.requireNonNull;

/**
 * Signals that a {@link Problem} was detected by the system. A {@link ProblemDiagnosis} describes the
 * details of the problem.
 * <p>
 * This is the base exception for the entire system.
 * All exceptions raised by the system should eventually be mapped into this exception or one of its sub-classes
 * before it is reported. Provides some metadata that is useful for error reporting.
 * </p>
 */
public class ProblemDetected extends RuntimeException implements IdentifiableProblem, HavingDiagnosis {

    private final UUID id;
    private final Instant occurredOn;

    // Effectively immutable:
    private ProblemDiagnosis diagnosed;

    protected ProblemDetected(ProblemDiagnosis diagnosed) {

        this.id = generateId();
        this.occurredOn = Instant.now();
        this.diagnosed = requireNonNull(diagnosed, "Problem diagnosis must not be null");
    }

    protected ProblemDetected(ProblemDetected other) {

        this.id = generateId();
        this.occurredOn = other.occurredOn;
        this.diagnosed = other.diagnosed;
    }

    private static UUID generateId() {

        return UUID.randomUUID();
    }

    public static ProblemDetected of(Problem problem) {

        return ProblemDetected.of(aProblemDiagnosis().withProblem(problem));
    }

    public static ProblemDetected of(ProblemDiagnosis reported) {

        return new ProblemDetected(reported);
    }

    @Override
    public ProblemDiagnosis diagnosis() {

        return diagnosed;
    }

    public final Problem problem() {

        return diagnosed.problem();
    }

    public final String title() {

        return problem().title();
    }

    public final String details() {

        return diagnosed.details();
    }

    public final Optional<String> debugMessage() {

        return Optional.ofNullable(diagnosed.debugMessage());
    }

    @Override
    public final UUID id() {

        return firstIdentifiableCause()
                .map(cause -> cause.equals(this) ? id : cause.id())
                .orElse(id);
    }

    @Override
    public final Instant occurredOn() {

        return firstIdentifiableCause()
                .map(cause -> cause.equals(this) ? occurredOn : cause.occurredOn())
                .orElse(occurredOn);
    }

    private Optional<IdentifiableProblem> firstIdentifiableCause() {

        var causeChain = ExceptionUtils.getThrowableList(this);
        Collections.reverse(causeChain);

        return causeChain.stream()
                         .filter(cause -> cause instanceof IdentifiableProblem)
                         .map(IdentifiableProblem.class::cast)
                         .findFirst();
    }

    @Override
    public final String getMessage() {

        return Stream.concat(debugMessage().stream(),
                             Stream.of(
                                     title(),
                                     details(),
                                     OffsetDateTime.ofInstant(occurredOn(), ZoneId.systemDefault()).toString(),
                                     id().toString()
                             )).collect(Collectors.joining(" - "));
    }

    public final Map<String, Object> context() {

        return new HashMap<>(diagnosed.context());
    }

    @Override
    public final synchronized Throwable getCause() {

        return diagnosed.cause();
    }

    protected ProblemDetected copy() {

        return new ProblemDetected(this);
    }

}
