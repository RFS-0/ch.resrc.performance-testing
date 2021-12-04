package ch.resrc.testing.use_cases.sort.ports.outbound;

import ch.resrc.testing.capabilities.validation.ValidationError;
import ch.resrc.testing.use_cases.sort.ports.documents.SortedDocument;
import ch.resrc.testing.use_cases.support.outbound_ports.authentication.Client;
import ch.resrc.testing.use_cases.support.outbound_ports.presentation.ErrorPresenter;

public interface SortedPresenter extends ErrorPresenter {

    void present(Client client, SortedDocument<?> sortedDocument);

    void present(Client client, ValidationError validationError);

}
