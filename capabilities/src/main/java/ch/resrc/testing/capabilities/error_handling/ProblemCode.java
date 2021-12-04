package ch.resrc.testing.capabilities.error_handling;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.*;

/**
 * Identifies a {@link Problem} with a short, simple code.
 * <p>
 * Automated clients use the code to unambiguously identify the error that our system has reported to them.
 * Human clients use this code to efficiently communicate about errors with other humans.
 * </p>
 */
public class ProblemCode {

    public static ProblemCode UNDEFINED = new ProblemCode(0);

    private final Integer value;

    static public ProblemCode code(Integer value) {
        return new ProblemCode(value);
    }

    private ProblemCode(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Problem code must be a positive number.");
        }
        this.value = value;
    }

    public Integer value() {
        return value;
    }

    @Override
    public String toString() {
        return StringUtils.leftPad(value.toString(), 4, "0");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProblemCode that = (ProblemCode) o;

        return new EqualsBuilder()
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .toHashCode();
    }
}
