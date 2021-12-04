package ch.resrc.testing.capabilities.error_handling.faults;

import ch.resrc.testing.capabilities.error_handling.*;

import static ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis.aProblemDiagnosis;

/**
 * Communication with a third party system or the client failed because
2 * there is a problem with the communication infrastructure. We typically don't know
 * whether our partner system received our request or what state it is in
 * or what its response was.
 */
public final class CommunicationFault extends Fault {


    private CommunicationFault(CommunicationFault other) {
        super(other);
    }

    private CommunicationFault(ProblemDiagnosis diagnosed) {
        super(diagnosed);
    }

    public static CommunicationFault of(ProblemDiagnosis diagnosed) {
        return new CommunicationFault(diagnosed);
    }

    public static CommunicationFault of(Problem detected) {
        return CommunicationFault.of(aProblemDiagnosis().withProblem(detected));
    }

    @Override
    protected CommunicationFault copy()
    {
        return new CommunicationFault(this);
    }

}
