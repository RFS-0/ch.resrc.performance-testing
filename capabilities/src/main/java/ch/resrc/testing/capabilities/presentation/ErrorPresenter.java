package ch.resrc.testing.capabilities.presentation;

import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.BusinessError;
import ch.resrc.testing.capabilities.validation.ValidationError;

/**
 * Presents errors to clients.
 */
public interface ErrorPresenter {

    /**
     * A system failure has occurred that was signaled by the supplied exception.
     * The presenter should present the failure as a system failure to the client.
     * <p>
     * The presenter takes care that no sensitive information about the error is disclosed
     * to clients.
     * </p>
     *
     * @param failure describes the failure that occurred
     */
    void presentSystemFailure(Client client, RuntimeException failure);

    /**
     * A business error has occurred for which the client is responsible or on which the
     * client might need to react. The presenter should present the error to the client.
     * The presenter presents the error exposing as much helpful information as possible.
     * <p>
     * This presentation method should normally only be called from the regular business flow.
     * Think twice if you call this method from an exception handler. You might unintentionally
     * disclose information about an error that is actually a system failure.
     * </p>
     *
     *
     * @param businessError describes the business error that has occurred.
     */
    void presentBusinessError(Client client, BusinessError businessError);

    ErrorPresenter THROWING_NO_OP = new ErrorPresenter() {

        @Override
        public void presentSystemFailure(Client client, RuntimeException failure) {
            throw failure;
        }

        @Override
        public void presentBusinessError(Client client, BusinessError businessError) {
            throw businessError.asException();
        }
    };

}
