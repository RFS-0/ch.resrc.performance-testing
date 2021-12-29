package ch.resrc.testing.use_cases.sort.ports.outbound;

import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.presentation.ErrorPresenter;
import ch.resrc.testing.use_cases.sort.ports.documents.SortedDocument;

public interface SortPresenter extends ErrorPresenter {

    <T extends Comparable<T>> void  present(Client client, SortedDocument<T> toBePresented);

}
