package ch.resrc.testing.domain;

import java.util.Objects;

/**
 * A domain value object whose value can be represented by a scalar primitive value of the
 * programming language.
 *
 * @param <T> the type of the domain value object
 * @param <V> the type of the primitive value
 */
public abstract class DomainPrimitive<T extends DomainPrimitive<T, V>, V> {

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T other = (T) o;
        return Objects.equals(getPrimitiveValue(), other.getPrimitiveValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrimitiveValue());
    }

    @Override
    public String toString() {
        return getPrimitiveValue() != null ? getPrimitiveValue().toString() : null;
    }

    /**
     * @return the primitive value representation of this object
     */
    protected abstract V getPrimitiveValue();
}
