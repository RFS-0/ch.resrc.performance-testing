package ch.resrc.testing.capabilities.error_handling;

import org.apache.commons.lang3.builder.*;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.*;

/**
 * Provides details about the circumstances and context of a {@link Problem} that has occurred in the system.
 * Collects all the information about a problem that might be used for error reporting.
 */
public class ProblemDiagnosis implements HavingDiagnosis {

    private Problem problem = GenericProblem.SYSTEM_FAILURE;
    private Throwable cause;
    private Map<String, Object> context = new HashMap<>();
    private String debugMessage;

    private ProblemDiagnosis() {}

    private ProblemDiagnosis(ProblemDiagnosis other) {
        this.problem = other.problem;
        this.cause = other.cause;
        this.context.putAll(other.context);
        this.debugMessage = other.debugMessage;
    }

    private ProblemDiagnosis copy() {
        return new ProblemDiagnosis(this);
    }

    public static ProblemDiagnosis aProblemDiagnosis() {
        return new ProblemDiagnosis();
    }

    public static ProblemDiagnosis diagnosis(Problem problem) {
        return new ProblemDiagnosis().withProblem(problem);
    }

    public static ProblemDiagnosis of(Problem problem) {
        return new ProblemDiagnosis().withProblem(problem);
    }

    public static ProblemDiagnosis diagnosis(String debugMessage) {
        return ProblemDiagnosis.of(debugMessage);
    }

    public static ProblemDiagnosis of(String debugMessage) {
        return new ProblemDiagnosis().withDebugMessage(debugMessage);
    }

    @Override
    public ProblemDiagnosis diagnosis() { return this; }

    public ProblemDiagnosis withProblem(Problem problem) {
        ProblemDiagnosis copy = copy();
        copy.problem = problem;
        return copy;
    }

    public ProblemDiagnosis withCause(Throwable cause) {
        ProblemDiagnosis copy = copy();
        copy.cause = cause;
        return copy;
    }

    public ProblemDiagnosis withContext(Map<String, Object> context) {
        ProblemDiagnosis copy = copy();
        copy.context = new HashMap<>(context);
        return copy;
    }

    @SafeVarargs
    public final ProblemDiagnosis withContext(Map.Entry<String, Object>... entries) {
        return this.withContext(Map.ofEntries(entries));
    }

    public final ProblemDiagnosis withContext(String key, Object value) {
        ProblemDiagnosis copy = copy();
        copy.context.put(key, value);
        return copy;
    }

    public ProblemDiagnosis withDebugMessage(String debugMessage) {
        ProblemDiagnosis copy = copy();
        copy.debugMessage = debugMessage;
        return copy;
    }

    public Problem problem() {
        return problem;
    }

    public Throwable cause() {
        return cause;
    }

    public Map<String, Object> context() {
        return new HashMap<>(context);
    }

    public String debugMessage() {
        return debugMessage;
    }

    public String details() {
        return StrSubstitutor.replace(problem.detailsTemplate(), this.context());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
