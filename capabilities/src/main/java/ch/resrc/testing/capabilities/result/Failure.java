package ch.resrc.testing.capabilities.result;

import java.util.*;
import java.util.function.*;

import static java.util.stream.Collectors.*;

/**
 * Represents the result of a failed operation.
 */
class Failure<T, E> extends Result<T, E> {


    private final List<E> errors;

    public Failure(List<E> errors) {this.errors = List.copyOf(errors);}

    @Override
    public boolean isSuccess() {return false;}

    @Override
    public boolean isFailure() {return true;}

    @Override
    public boolean isEmpty() {return false;}

    @Override
    public T value() {throw new NoSuchElementException("This is a Failure result. There is no result value.");}

    @Override
    public List<E> errors() {return List.copyOf(this.errors);}

    @Override
    public <U> Result<U, E> map(Function<T, U> f) {return new Failure<>(this.errors);}

    @Override
    public <U> Result<U, E> flatMap(Function<T, Result<U, E>> f) {return new Failure<>(this.errors);}

    @Override
    public <EE> Result<T, EE> mapErrors(Function<? super E, EE> f) {

        List<EE> mappedErrors = this.errors.stream().map(f).collect(toList());
        return new Failure<>(mappedErrors);
    }

    @Override
    public <EE> Result<T, EE> handleErrors(ErrorHandling<T, E, EE> errorHandling) throws RuntimeException {

        return errorHandling.apply(this.errors());
    }

    @Override
    public T getOrThrow(Function<List<E>, ? extends RuntimeException> errorMap) {

        throw errorMap.apply(this.errors);
    }

    @Override
    public Result<T, E> ifFailureThrow(Function<List<E>, ? extends RuntimeException> errorMap) {throw errorMap.apply(this.errors);}

    @Override
    public <U extends T> Result<T, E> recover(U alternativeValue) {return Result.success(alternativeValue);}

    @Override
    public <U extends T> Result<T, E> recoverEmpty(Supplier<U> alternative) {return this;}

    @Override
    public Result<T, E> ifEmptySwitch(Supplier<Result<T, E>> alternative) {return this;}

    @Override
    public Result<T, E> failIfSuccess(E error) {

        return this;
    }

    @Override
    public <U> Result<U, E> thenResultOf(Supplier<Result<U, E>> supplier) {return new Failure<>(this.errors);}

    @Override
    public Result<T, E> effect(Runnable effect) {return this;}

    @Override
    public Result<T, E> effect(Consumer<T> effect) {return this;}

}
