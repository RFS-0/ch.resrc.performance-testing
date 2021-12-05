package ch.resrc.testing.use_cases.sort.ports.outbound;

import ch.resrc.testing.capabilities.validation.ValidationError;
import ch.resrc.testing.use_cases.sort.ports.documents.SortedDocument;
import ch.resrc.testing.use_cases.support.outbound_ports.authentication.Client;
import ch.resrc.testing.use_cases.support.outbound_ports.presentation.ErrorPresenter;

public interface SortPresenter<T extends Comparable<T>> extends ErrorPresenter {

    void present(Client client, SortedDocument<T> toBePresented);

    void present(Client client, ValidationError validationError);

}
