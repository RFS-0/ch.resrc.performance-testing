package ch.resrc.testing.adapters.rest.errorhandling;


import ch.resrc.testing.adapters.rest.output.HavingPresentation;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.Try;
import ch.resrc.testing.capabilities.error_handling.faults.OurFault;
import ch.resrc.testing.capabilities.presentation.ErrorPresenter;
import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.capabilities.validation.*;
import ch.resrc.testing.use_cases.support.habits.errorhandling.Blame;

import java.util.function.*;

import static ch.resrc.testing.capabilities.functional.ForEach.forEach;

/**
 * Represents the standard flow by which our REST endpoints invoke a use case.
 * <p>
 * Ensures that input validation errors are reported properly and audits any errors and exceptions
 * that the use case might raise.
 * </p>
 * <p>
 * All REST endpoints should use this template to invoke the respective use case.
 * </p>
 */
public class UseCaseTry {

    private final Consumer<RuntimeException> errorAudit;

    public UseCaseTry(Consumer<RuntimeException> errorAudit) {this.errorAudit = errorAudit;}


    public <T extends Validatable<T>> Execution<T, ?> withInput(T input) {

        return new Execution<>(input, null);
    }

    public <P extends HavingPresentation & ErrorPresenter> Execution<NoInput, P> withOutput(P output) {

        return new Execution<>(new NoInput(), output);
    }

    public class Execution<IN extends Validatable<IN>, OUT extends HavingPresentation & ErrorPresenter> {

        private final IN input;
        private final OUT output;

        Execution(IN input, OUT output) {

            this.input = input;
            this.output = output;
        }

        public <OUT2 extends HavingPresentation & ErrorPresenter>
        Execution<IN, OUT2> withOutput(OUT2 output) {

            return new Execution<>(this.input, output);
        }

        public OUT invoke(BiConsumer<IN, OUT> useCase) {

            input.validated()
                 .mapErrors(Blame::asClientFault)
                 .failureEffect(forEach(errorAudit::accept))
                 .failureEffect(forEach((error) -> output.presentBusinessError(input.client(), error)))
                 .effect(audited(() -> useCase.accept(input, output)));

            return output;
        }

        public OUT invoke(Consumer<OUT> useCase) {

            return this.invoke((in, out) -> useCase.accept(out));
        }

        private Runnable audited(Runnable useCase) {

            return () -> Try.ofVoid(useCase)
                            .onSuccess(() -> {
                                if (output.isPresentationMissing()) {
                                    throw OurFault.of(RestProblem.USE_CASE_RESULT_MISSING);
                                }
                            })
                            .onFailure((error) -> output.presentSystemFailure(input.client(), error))
                            .onFailure(errorAudit);
        }
    }

    static class NoInput implements Validatable<NoInput> {

        @Override
        public Client client() {
            return Client.anonymous();
        }

        @Override
        public Result<NoInput, ValidationError> validated() {return Result.success(this);}
    }
}
