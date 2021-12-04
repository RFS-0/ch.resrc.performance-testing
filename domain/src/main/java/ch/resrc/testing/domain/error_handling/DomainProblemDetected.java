package ch.resrc.testing.domain.error_handling;

import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis;

import java.util.List;

import static ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis.aProblemDiagnosis;

public class DomainProblemDetected extends ProblemDetected {

    private DomainProblemDetected(ProblemDiagnosis diagnosed) {
        super(diagnosed);
    }

    private DomainProblemDetected(DomainProblemDetected other) {
        super(other);
    }

    public static DomainProblemDetected of(DomainProblem detected) {
        return DomainProblemDetected.of(aProblemDiagnosis().withProblem(detected));
    }

    public static DomainProblemDetected of(ProblemDiagnosis diagnosis) {
        return new DomainProblemDetected(diagnosis);
    }

    public static DomainProblemDetected of(List<ProblemDiagnosis> problems) {
        return DomainProblemDetected.of(problems.get(0));
    }

    public static DomainProblemDetected ofErrorChoices(List<? extends ProblemEnumeration> problems) {
        return DomainProblemDetected.of(problems.get(0).diagnosis());
    }

    @Override
    protected DomainProblemDetected copy() {
        return new DomainProblemDetected(this);
    }
}
