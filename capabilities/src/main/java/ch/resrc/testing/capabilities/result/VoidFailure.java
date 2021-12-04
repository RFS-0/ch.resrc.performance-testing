package ch.resrc.testing.capabilities.result;

import java.util.*;
import java.util.function.*;

/**
 * Represents the result of a failed void operation.
 */
class VoidFailure<E> extends Result<Void, E> {

    private final Failure<Void, E> failure;

    public VoidFailure(List<E> errors) {

        this.failure = new Failure<>(errors);
    }

    @Override
    public boolean isSuccess() {return false;}

    @Override
    public boolean isFailure() {return true;}

    @Override
    public boolean isEmpty() {return false;}

    @Override
    public Void value() throws NoSuchElementException {return failure.value();}

    @Override
    public List<E> errors() throws NoSuchElementException {return failure.errors();}

    @Override
    public VoidFailure<E> ifFailureThrow(Function<List<E>, ? extends RuntimeException> errorMap) {

        failure.ifFailureThrow(errorMap);
        return this;
    }

    @Override
    public <U> Result<U, E> thenResultOf(Supplier<Result<U, E>> supplier) {return failure.thenResultOf(supplier);}

    @Override
    public VoidFailure<E> effect(Runnable effect) {return this;}

    @Override
    public VoidFailure<E> effect(Consumer<Void> effect) {return this;}

    @Override
    public <U> Result<U, E> map(Function<Void, U> f) {

        throw unsupportedOperation("VoidFailure");
    }

    @Override
    public <U> Result<U, E> flatMap(Function<Void, Result<U, E>> f) {

        throw unsupportedOperation("VoidFailure");
    }

    @Override
    public <EE> VoidFailure<EE> mapErrors(Function<? super E, EE> f) {

        List<EE> mappedErrors = failure.mapErrors(f).errors();
        return new VoidFailure<>(mappedErrors);
    }

    @Override
    public <EE> Result<Void, EE> handleErrors(ErrorHandling<Void, E, EE> errorHandling) throws RuntimeException {

        return errorHandling.apply(this.errors());
    }

    @Override
    public Void getOrThrow(Function<List<E>, ? extends RuntimeException> errorMap) {

        throw unsupportedOperation("VoidFailure");
    }

    @Override
    public Result<Void, E> recover(Void alternativeValue) {

        throw unsupportedOperation("VoidFailure");
    }

    @Override
    public <U extends Void> Result<Void, E> recoverEmpty(Supplier<U> alternative) {return this;}

    @Override
    public Result<Void, E> ifEmptySwitch(Supplier<Result<Void, E>> alternative) {return this;}

    @Override
    public Result<Void, E> failIfSuccess(E error) {

        return this;
    }
}
