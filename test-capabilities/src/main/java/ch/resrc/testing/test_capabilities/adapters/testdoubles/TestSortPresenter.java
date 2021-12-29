package ch.resrc.testing.test_capabilities.adapters.testdoubles;

import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.validation.ValidationError;
import ch.resrc.testing.use_cases.sort.ports.documents.SortedDocument;
import ch.resrc.testing.use_cases.sort.ports.outbound.SortPresenter;

public class TestSortPresenter extends TestErrorPresenter implements SortPresenter {

    private Client client;
    private SortedDocument<?> presented;
    private ValidationError validationError;

    @Override
    public <T extends Comparable<T>> void present(Client client, SortedDocument<T> toBePresented) {
        this.client = client;
        this.presented = toBePresented;
    }

    public Client client() {
        return client;
    }

    public SortedDocument<?> presented() {
        return presented;
    }

    public ValidationError validationError() {
        return validationError;
    }
}
