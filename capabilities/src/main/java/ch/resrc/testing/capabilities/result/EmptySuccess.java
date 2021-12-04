package ch.resrc.testing.capabilities.result;

import java.util.*;
import java.util.function.*;

class EmptySuccess<T, E> extends Result<T, E> {

    @Override
    public boolean isSuccess() {return true;}

    @Override
    public boolean isEmpty() {return true;}

    @Override
    public boolean isFailure() {return false;}

    @Override
    public T value() throws NoSuchElementException {

        throw new NoSuchElementException("This is an empty result.");
    }

    @Override
    public List<E> errors() throws NoSuchElementException {

        return List.of();
    }

    @Override
    public <U> Result<U, E> map(Function<T, U> f) {

        return Result.empty();
    }

    @Override
    public <U> Result<U, E> flatMap(Function<T, Result<U, E>> f) {

        return Result.empty();
    }

    @Override
    public <EE> Result<T, EE> mapErrors(Function<? super E, EE> f) {

        return Result.empty();
    }

    @Override
    public <EE> Result<T, EE> handleErrors(ErrorHandling<T, E, EE> errorHandling) throws RuntimeException {

        return Result.empty();
    }

    @Override
    public T getOrThrow(Function<List<E>, ? extends RuntimeException> errorMap) throws RuntimeException {

        throw new NoSuchElementException("This is an empty result.");
    }

    @Override
    public Result<T, E> ifFailureThrow(Function<List<E>, ? extends RuntimeException> errorMap) {

        return Result.empty();
    }

    @Override
    public <U extends T> Result<T, E> recover(U alternativeValue) {

        return Result.success(alternativeValue);
    }

    @Override
    public <U extends T> Result<T, E> recoverEmpty(Supplier<U> alternative) {

        return Result.success(alternative.get());
    }

    @Override
    public Result<T, E> ifEmptySwitch(Supplier<Result<T, E>> alternative) {

        return alternative.get().map(x -> x);
    }

    @Override
    public Result<T, E> failIfSuccess(E error) {

        return Result.failure(error);
    }

    @Override
    public <U> Result<U, E> thenResultOf(Supplier<Result<U, E>> supplier) {

        return supplier.get();
    }

    @Override
    public Result<T, E> effect(Runnable effect) {

        effect.run();
        return this;
    }

    @Override
    public Result<T, E> effect(Consumer<T> effect) {

        throw new NoSuchElementException("This is an empty result.");
    }

}
