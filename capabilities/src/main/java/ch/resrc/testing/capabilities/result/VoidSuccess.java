package ch.resrc.testing.capabilities.result;

import java.util.*;
import java.util.function.*;

/**
 * Represents the result of a successful void operation.
 */
class VoidSuccess<E> extends Result<Void, E> {


    @Override
    public boolean isSuccess() {return true;}

    @Override
    public boolean isFailure() {return false;}

    @Override
    public boolean isEmpty() {return false;}

    @Override
    public Void value() {throw new NoSuchElementException("This is a VoidSuccess result. There is no value.");}

    @Override
    public List<E> errors() {

        return List.of();
    }

    @Override
    public <U> Result<U, E> map(Function<Void, U> f) {

        throw unsupportedOperation("VoidSuccess");
    }

    @Override
    public <U> Result<U, E> flatMap(Function<Void, Result<U, E>> f) {

        throw unsupportedOperation("VoidSuccess");
    }

    @Override
    public <EE> VoidSuccess<EE> mapErrors(Function<? super E, EE> f) {return new VoidSuccess<>();}

    @Override
    public <EE> Result<Void, EE> handleErrors(ErrorHandling<Void, E, EE> errorHandling) throws RuntimeException {

        return Result.voidSuccess();
    }

    @Override
    public Void getOrThrow(Function<List<E>, ? extends RuntimeException> errorMap) {

        throw unsupportedOperation("VoidSuccess");
    }


    @Override
    public VoidSuccess<E> ifFailureThrow(Function<List<E>, ? extends RuntimeException> errorMap) {return this;}

    @Override
    public VoidSuccess<E> recover(Void alternativeValue) {

        throw unsupportedOperation("VoidSuccess");
    }

    @Override
    public <U extends Void> Result<Void, E> recoverEmpty(Supplier<U> alternative) {return this;}

    @Override
    public Result<Void, E> ifEmptySwitch(Supplier<Result<Void, E>> alternative) {return this;}

    @Override
    public Result<Void, E> failIfSuccess(E error) {

        return Result.voidFailure(error);
    }

    @Override
    public <U> Result<U, E> thenResultOf(Supplier<Result<U, E>> supplier) {return supplier.get();}

    @Override
    public VoidSuccess<E> effect(Runnable effect) {

        effect.run();
        return this;
    }

    @Override
    public VoidSuccess<E> effect(Consumer<Void> effect) {

        throw unsupportedOperation("VoidSuccess");
    }

}
