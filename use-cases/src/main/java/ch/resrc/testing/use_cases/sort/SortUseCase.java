package ch.resrc.testing.use_cases.sort;

import ch.resrc.testing.capabilities.error_handling.faults.OurFault;
import ch.resrc.testing.capabilities.events.Notifiable;
import ch.resrc.testing.capabilities.functional.VoidMatch;
import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.capabilities.validation.ValidationError;
import ch.resrc.testing.domain.RedBlackTreeSort;
import ch.resrc.testing.use_cases.sort.ports.events.Sorted;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;
import ch.resrc.testing.use_cases.sort.ports.outbound.SortedPresenter;
import ch.resrc.testing.use_cases.support.habits.events.Forum;
import ch.resrc.testing.use_cases.support.outbound_ports.authentication.Client;

import java.util.List;

import static ch.resrc.testing.capabilities.functional.ForEach.forEach;
import static ch.resrc.testing.capabilities.functional.VoidMatch.DefaultIgnore;
import static ch.resrc.testing.use_cases.support.habits.errorhandling.Blame.isClientFault;
import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

public class SortUseCase<T extends Comparable<T>> implements Sort<T> {

    @Override
    public void invoke(Input<T> input, SortedPresenter presenter) {
        var forum = new Forum();

        UserInterface ui = new UserInterface(input.client(), presenter);
        forum.events().subscribe(ui);

        Workflow<T> workflow = new Workflow<>(forum);

        workflow.sortList(input.toBeSorted());
    }

    static class Workflow<T extends Comparable<T>> {

        final Forum forum;

        void sortList(List<T> unsorted) {
            new ListUnderSortation<>(unsorted)
                    .sort(new RedBlackTreeSort<>())
                    .effect((SortedList<T> x) -> x.publishCreationSuccessTo(forum))
                    .failureEffect(forEach(forum::publish));
        }

        Workflow(Forum forum) {
            this.forum = forum;
        }
    }

    static class ListUnderSortation<T extends Comparable<T>> {

        final List<T> unsorted;

        Result<SortedList<T>, OurFault> sort(ch.resrc.testing.domain.Sort<T> sort) {
            List<T> sorted = sort.apply(unsorted);
            return Result.success(new SortedList<>(sorted));
        }

        ListUnderSortation(List<T> unsorted) {
            this.unsorted = unsorted;
        }
    }

    static class SortedList<T extends Comparable<T>> {

        final List<T> sorted;

        void publishCreationSuccessTo(Notifiable receiver) {
            receiver.on(Sorted.of(sorted));
        }

        public SortedList(List<T> sorted) {
            this.sorted = sorted;
        }
    }

    static class UserInterface implements Notifiable {

        final Client client;
        final SortedPresenter presenter;

        @Override
        public void on(Object notification) {
            Match(notification).of(
                    VoidMatch.Case($(instanceOf(Sorted.class)), sortedList -> presenter.present(client, sortedList.document())),
                    VoidMatch.Case($(instanceOf(ValidationError.class)), validationError -> presenter.present(client, validationError)),
                    VoidMatch.Case($(isClientFault()), error -> presenter.presentBusinessError(client, error)),
                    DefaultIgnore()
            );
        }

        UserInterface(Client client, SortedPresenter presenter) {
            this.client = client;
            this.presenter = presenter;
        }
    }
}
