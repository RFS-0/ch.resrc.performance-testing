package ch.resrc.testing.capabilities.error_handling.faults;

import ch.resrc.testing.capabilities.error_handling.*;

import static ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis.aProblemDiagnosis;

/**
 * Signals that an invocation of a third party system failed and the third party blames our system for having
 * sent a bad request.
 */
public final class TheyBlameUs extends Fault {


    private TheyBlameUs(ProblemDiagnosis diagnosed) {
        super(diagnosed);
    }

    private TheyBlameUs(TheyBlameUs other) {
        super(other);
    }

    public static TheyBlameUs of(ProblemDiagnosis diagnosed) {
        return new TheyBlameUs(diagnosed);
    }

    public static TheyBlameUs of(Problem detected) {
        return TheyBlameUs.of(aProblemDiagnosis().withProblem(detected));
    }

    @Override
    protected TheyBlameUs copy() {
        return new TheyBlameUs(this);
    }
}
