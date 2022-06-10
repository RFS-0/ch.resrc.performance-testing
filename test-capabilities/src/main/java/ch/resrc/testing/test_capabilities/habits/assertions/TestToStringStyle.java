package ch.resrc.testing.test_capabilities.habits.assertions;

import org.apache.commons.lang3.builder.ToStringStyle;

public class TestToStringStyle extends ToStringStyle {

    public TestToStringStyle() {
        super();
        this.setUseClassName(false);
        this.setUseIdentityHashCode(false);
        this.setUseFieldNames(false);
        this.setContentStart("|");
        this.setFieldSeparator("|");
        this.setContentEnd("|");
    }

}
