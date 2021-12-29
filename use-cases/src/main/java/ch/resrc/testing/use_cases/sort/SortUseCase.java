package ch.resrc.testing.use_cases.sort;

import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis;
import ch.resrc.testing.capabilities.error_handling.faults.*;
import ch.resrc.testing.capabilities.events.Notifiable;
import ch.resrc.testing.capabilities.functional.VoidMatch;
import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.capabilities.validation.ValidationError;
import ch.resrc.testing.domain.Sorter;
import ch.resrc.testing.domain.error_handling.DomainProblem;
import ch.resrc.testing.use_cases.sort.ports.events.Sorted;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;
import ch.resrc.testing.use_cases.sort.ports.outbound.SortPresenter;
import ch.resrc.testing.use_cases.support.habits.events.Forum;

import java.util.List;

import static ch.resrc.testing.capabilities.functional.ForEach.forEach;
import static ch.resrc.testing.capabilities.functional.VoidMatch.DefaultIgnore;
import static ch.resrc.testing.use_cases.support.habits.errorhandling.Blame.isClientFault;
import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

public class SortUseCase implements Sort {

    private final Sorter sorter;

    private SortUseCase(Sorter sorter) {
        this.sorter = sorter;
    }

    public static SortUseCase create(Sorter sorter) {
        return new SortUseCase(sorter);
    }

    @Override
    public <T extends Comparable<T>> void invoke(Input<T> input, SortPresenter presenter) {
        var forum = new Forum();

        UserInterface<T> ui = new UserInterface<>(input.client(), presenter);
        forum.events().subscribe(ui);

        Workflow<T> workflow = new Workflow<>(sorter, forum);

        workflow.sortList(input.toBeSorted());
    }

    static class Workflow<T extends Comparable<T>> {

        final Forum forum;
        Sorter sorter;

        void sortList(List<T> unsorted) {
            new ListUnderSortation<>(unsorted)
                    .sort(sorter)
                    .effect((SortedList<T> x) -> x.publishCreationSuccessTo(forum))
                    .failureEffect(forEach(forum::publish));
        }

        Workflow(Sorter sorter, Forum forum) {
            this.sorter = sorter;
            this.forum = forum;
        }
    }

    static class ListUnderSortation<T extends Comparable<T>> {

        final List<T> unsorted;

        Result<SortedList<T>, OurFault> sort(Sorter sorter) {
            List<T> sorted = sorter.apply(unsorted);
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

    static class UserInterface<T extends Comparable<T>> implements Notifiable {

        final Client client;
        final SortPresenter presenter;

        @Override
        public void on(Object notification) {
            Match(notification).of(
                    VoidMatch.Case(
                            $(instanceOf(Sorted.class)),
                            (Sorted<T> sorted) -> presenter.present(client, sorted.document())
                    ),
                    VoidMatch.Case(
                            $(instanceOf(ValidationError.class)),
                            validationError -> presenter.presentBusinessError(
                                    client,
                                    ClientFault.of(
                                            ProblemDiagnosis
                                                    .of(DomainProblem.INVARIANT_VIOLATED)
                                                    .withContext("message", validationError.errorMessage())
                                    )
                            )
                    ),
                    VoidMatch.Case(
                            $(isClientFault()),
                            error -> presenter.presentBusinessError(client, error)
                    ),
                    DefaultIgnore()
            );
        }

        UserInterface(Client client, SortPresenter presenter) {
            this.client = client;
            this.presenter = presenter;
        }
    }
}
