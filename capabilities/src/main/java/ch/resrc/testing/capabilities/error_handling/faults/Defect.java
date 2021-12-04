package ch.resrc.testing.capabilities.error_handling.faults;


import ch.resrc.testing.capabilities.error_handling.*;

import java.util.List;

import static ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis.aProblemDiagnosis;

/**
 * Signals that the system failed due to a programming error in the application. Such problems
 * usually require bug fixing by developers.
 *
 * <p>Throw this exception if a code path is executed that should never be executed if the application
 * was programmed correctly. If the problem of a temporary nature or not necessarily a programming error,
 * throw {@link OurFault} instead.</p>
 */
public final class Defect extends Fault {

    private final static ProblemDiagnosis systemFailure = aProblemDiagnosis().withProblem(GenericProblem.SYSTEM_FAILURE);

    private Defect(Defect other) {
        super(other);
    }

    private Defect(ProblemDiagnosis diagnosed) {
        super(diagnosed);
    }

    public static Defect of(ProblemDiagnosis diagnosed) {
        return new Defect(diagnosed);
    }

    public static Defect of(String debugMessage) {
        return Defect.of(systemFailure.withDebugMessage(debugMessage));
    }

    public static Defect of(List<String> errorMessages) {
        String debugMessage = String.join("; ", errorMessages);
        return Defect.of(systemFailure.withDebugMessage(debugMessage));
    }

    public static Defect of(Throwable cause) {
        return Defect.of(systemFailure.withDebugMessage(cause.getMessage()).withCause(cause));
    }

    @Override
    protected Defect copy() {
        return new Defect(this);
    }
}
