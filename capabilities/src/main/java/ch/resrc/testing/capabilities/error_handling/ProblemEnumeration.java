package ch.resrc.testing.capabilities.error_handling;

import ch.resrc.testing.capabilities.error_handling.faults.Defect;

import java.util.List;

import static java.lang.String.format;

/**
 * Signals an error where the reason for the error is a particular {@link Problem}
 * out of a fixed set of possible {@code Problem}s. The {@link #possibleProblems()} method returns the possible
 * problems.
 * Cannot be instantiated unless the signaled problem is one of the possible problems.
 * <p>
 * This class is essentially a surrogate for union types that exist in some functional programming languages.
 * It allows us to model the possible errors that an operation can raise as a single error object. This object
 * explicitly communicates the possible problems that it can represent. In a functional programming language such
 * errors
 * could be modelled more elegantly with union types.
 * </p>
 */
public abstract class ProblemEnumeration implements HavingDiagnosis, HavingExceptionRepresentation {

    private final ProblemDiagnosis signaledProblem;

    protected ProblemEnumeration(ProblemDiagnosis signaledProblem) {
        requireOneOf(this.possibleProblems(), signaledProblem.problem(), signaledProblem);
        this.signaledProblem = signaledProblem;
    }

    private static void requireOneOf(List<Problem> possibleProblems, Problem problem, ProblemDiagnosis problemDiagnosis) {

        if (!possibleProblems.contains(problem)) {
            throw Defect.of(format("Undeclared problem choice: <%s - %s - %s>",
                    problem.getClass().getSimpleName(),
                    problem.title(),
                    problemDiagnosis.details()));
        }
    }

    /**
     * @return the possible problems that this object can signal
     */
    protected abstract List<Problem> possibleProblems();

    /**
     * @return the {@link ProblemDiagnosis} that describes the problem that this object signals.
     */
    @Override
    public ProblemDiagnosis diagnosis() {
        return signaledProblem;
    }

    @Override
    public ProblemDetected asException() {

        return ProblemDetected.of(this.diagnosis());
    }
}
