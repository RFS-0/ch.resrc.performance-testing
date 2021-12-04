package ch.resrc.testing.capabilities.result;

import java.util.*;
import java.util.function.*;

/**
 * Represents the result of a successful operation.
 */
class Success<T, E> extends Result<T, E> {

    private final T value;

    public Success(T value) {this.value = value;}

    @Override
    public boolean isSuccess() {return true;}

    @Override
    public boolean isFailure() {return false;}

    @Override
    public boolean isEmpty() {return false;}

    @Override
    public T value() {return this.value;}

    @Override
    public List<E> errors() {

        return List.of();
    }

    @Override
    public <U> Result<U, E> map(Function<T, U> f) {

        return Result.success(f.apply(this.value));
    }

    @Override
    public <U> Result<U, E> flatMap(Function<T, Result<U, E>> f) {

        return f.apply(this.value);
    }

    @Override
    public <EE> Result<T, EE> mapErrors(Function<? super E, EE> f) {return Result.success(this.value);}

    @Override
    public <EE> Result<T, EE> handleErrors(ErrorHandling<T, E, EE> errorHandling) throws RuntimeException {

        return Result.success(this.value());
    }

    @Override
    public T getOrThrow(Function<List<E>, ? extends RuntimeException> errorMap) {return this.value;}


    @Override
    public Result<T, E> ifFailureThrow(Function<List<E>, ? extends RuntimeException> errorMap) {return this;}

    @Override
    public <U extends T> Result<T, E> recover(U alternativeValue) {return this;}

    @Override
    public <U extends T> Result<T, E> recoverEmpty(Supplier<U> alternative) {return this;}

    @Override
    public Result<T, E> ifEmptySwitch(Supplier<Result<T, E>> alternative) {return this;}

    @Override
    public Result<T, E> failIfSuccess(E error) {

        return Result.failure(error);
    }

    @Override
    public <U> Result<U, E> thenResultOf(Supplier<Result<U, E>> supplier) {return supplier.get();}

    @Override
    public Result<T, E> effect(Runnable effect) {

        effect.run();
        return this;
    }

    @Override
    public Result<T, E> effect(Consumer<T> effect) {

        effect.accept(value);
        return this;
    }

}
