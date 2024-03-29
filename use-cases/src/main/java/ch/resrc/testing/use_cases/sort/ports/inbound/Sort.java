package ch.resrc.testing.use_cases.sort.ports.inbound;

import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.use_cases.sort.ports.outbound.SortPresenter;

import java.util.List;

public interface Sort {

    <T extends Comparable<T>> void invoke(Input<T> input, SortPresenter presenter);

        class Input<T> {

            private final Client client;
            private final List<T> toBeSorted;

            public Input(Client client, List<T> toBeSorted) {
                this.client = client;
                this.toBeSorted = toBeSorted;
            }

            public Client client() {
                return client;
            }

            public List<T> toBeSorted() {
                return toBeSorted;
            }
        }
}