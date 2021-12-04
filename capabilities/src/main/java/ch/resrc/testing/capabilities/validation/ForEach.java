package ch.resrc.testing.capabilities.validation;


import ch.resrc.testing.capabilities.result.*;

import java.util.*;
import java.util.function.*;

import static ch.resrc.testing.capabilities.functional.Mapped.mapIndexed;
import static ch.resrc.testing.capabilities.validation.ValidationErrorModifier.withIndex;

public class ForEach<T, E> implements Validation<Collection<T>, E> {

    private final Function<T, Validation<T, E>> getValidationForElement;

    public ForEach(Validation<T, E> elementValidation) {

        this.getValidationForElement = x -> elementValidation;
    }

    public ForEach(Function<T, Validation<T, E>> getValidationForElement) {

        this.getValidationForElement = getValidationForElement;
    }

    @Override
    public Result<Collection<T>, E> applyTo(Collection<T> toBeValidated) {

        if (toBeValidated == null) {
            return Result.success(null);
        }

        List<Result<?, E>> allValidated = mapIndexed(toBeValidated, (BiFunction<? super T, Integer, Result<?, E>>) this::validateElement);

        return Precondition.of(allValidated).thenValueOf(() -> toBeValidated);
    }

    private Result<T, E> validateElement(T elementToBeValidated, Integer index) {

        return getValidationForElement.apply(elementToBeValidated)
                                      .applyTo(elementToBeValidated)
                                      .mapErrors(withIndex(index));
    }

}
