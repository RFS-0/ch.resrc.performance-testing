package ch.resrc.testing.test_capabilities.adapters.testdoubles;

import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.use_cases.support.outbound_ports.authentication.Client;
import ch.resrc.testing.use_cases.support.outbound_ports.presentation.ErrorPresenter;
import org.apache.commons.lang3.builder.*;

import java.util.*;

public class TestErrorPresenter implements ErrorPresenter {

    private final List<RuntimeException> presentedFailures = new ArrayList<>();
    private final List<ProblemDetected> presentedBusinessErrors = new ArrayList<>();

    @Override
    public void presentSystemFailure(Client client, RuntimeException failure) {

        presentedFailures.add(failure);
    }

    public Result<Void, ProblemDetected> presentedBusinessErrorResult() {

        if (this.presentedBusinessErrors.isEmpty()) {
            return Result.voidSuccess();
        } else {
            return Result.voidFailure(this.presentedBusinessErrors);
        }
    }

    public List<RuntimeException> presentedFailures() {
        return this.presentedFailures;
    }

    public Result<Void, RuntimeException> presentedFailureResult() {

        if (presentedFailures.isEmpty()) {
            return Result.voidSuccess();
        } else {
            return Result.failure(presentedFailures);
        }
    }

    public RuntimeException singleFailure() {

        if (presentedFailures.isEmpty()) {
            throw new NoSuchElementException("No failures were presented.");
        }

        if (presentedFailures.size() > 1) {
            throw new IllegalStateException("More than one failure was presented: " + this);
        }

        return presentedFailures.get(0);
    }

    @Override
    public void presentBusinessError(Client client, BusinessError businessError) {

        presentedBusinessErrors.add(businessError.asException());
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("presentedFailures", presentedFailures)
                .toString();
    }
}
