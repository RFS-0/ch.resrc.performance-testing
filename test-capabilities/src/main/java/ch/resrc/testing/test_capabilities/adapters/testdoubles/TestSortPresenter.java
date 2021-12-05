package ch.resrc.testing.test_capabilities.adapters.testdoubles;

import ch.resrc.testing.capabilities.validation.ValidationError;
import ch.resrc.testing.use_cases.sort.ports.documents.SortedDocument;
import ch.resrc.testing.use_cases.sort.ports.outbound.SortPresenter;
import ch.resrc.testing.use_cases.support.outbound_ports.authentication.Client;

public class TestSortPresenter<T extends Comparable<T>> extends TestErrorPresenter implements SortPresenter<T> {

    private Client client;
    private SortedDocument<T> presented;
    private ValidationError validationError;

    @Override
    public void present(Client client, SortedDocument<T> toBePresented) {
        this.client = client;
        this.presented = toBePresented;
    }

    @Override
    public void present(Client client, ValidationError validationError) {
        this.client = client;
        this.validationError = validationError;
    }

    public Client client() {
        return client;
    }

    public SortedDocument<T> presented() {
        return presented;
    }

    public ValidationError validationError() {
        return validationError;
    }
}
