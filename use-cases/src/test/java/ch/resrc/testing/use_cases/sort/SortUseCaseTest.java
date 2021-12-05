package ch.resrc.testing.use_cases.sort;

import ch.resrc.testing.domain.value_objects.ClientId;
import ch.resrc.testing.test_capabilities.adapters.testdoubles.TestSortPresenter;
import ch.resrc.testing.test_capabilities.habits.use_cases.*;
import ch.resrc.testing.test_capabilities.testbed.TestBed;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;
import ch.resrc.testing.use_cases.support.outbound_ports.authentication.Client;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SortUseCaseTest

        implements

        SortHabits {

    @Nested
    class If_requested_to_sort_a_list {

        @Test
        void it_sorts_the_list() {
            new TestBed.Context() {{
                // given:
                Sort.Input<Integer> input = SortInputSpec.<Integer>createSortInput()
                        .client(Client.of(ClientId.of("e5e81b85-0f2f-43a1-b59a-63fcbc7eeab0")))
                        .toBeSorted(List.of(10, 1, 6, 5, 7, 2, 8, 3, 4, 9))
                        .asInput();
                var useCase = newSortUseCase();
                var presenter = new TestSortPresenter<Integer>();

                // when:
                useCase.invoke(input, presenter);

                // then
                List<Integer> presentedList = presenter.presented().sortedList();
                assertThat(presentedList).isEqualTo(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
            }};
        }

        @Test
        void it_presents_the_sorted_list_to_the_client() {
            // given:
            Sort.Input<Integer> input = SortInputSpec.<Integer>createSortInput()
                    .client(Client.of(ClientId.of("e5e81b85-0f2f-43a1-b59a-63fcbc7eeab0")))
                    .toBeSorted(List.of(10, 1, 6, 5, 7, 2, 8, 3, 4, 9))
                    .asInput();
            var useCase = newSortUseCase();
            var presenter = new TestSortPresenter<Integer>();

            // when:
            useCase.invoke(input, presenter);

            // then
            Client client = presenter.client();
            assertThat(client.id()).isEqualTo(ClientId.of("e5e81b85-0f2f-43a1-b59a-63fcbc7eeab0"));
            List<Integer> presentedList = presenter.presented().sortedList();
            assertThat(presentedList).isEqualTo(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        }
    }
}