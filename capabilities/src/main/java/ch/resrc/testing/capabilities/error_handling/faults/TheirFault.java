package ch.resrc.testing.capabilities.error_handling.faults;

import ch.resrc.testing.capabilities.error_handling.*;

import static ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis.aProblemDiagnosis;

/**
 * Signals that a third party system failed or misbehaved.
 */
public final class TheirFault extends Fault {

    private TheirFault(TheirFault other) {
        super(other);
    }

    private TheirFault(ProblemDiagnosis diagnosed) {
        super(diagnosed);
    }

    public static TheirFault of(ProblemDiagnosis diagnosed) {
        return new TheirFault(diagnosed);
    }

    public static TheirFault of(Problem detected) {
        return TheirFault.of(aProblemDiagnosis().withProblem(detected));
    }

    @Override
    protected TheirFault copy() {
        return new TheirFault(this);
    }
}
