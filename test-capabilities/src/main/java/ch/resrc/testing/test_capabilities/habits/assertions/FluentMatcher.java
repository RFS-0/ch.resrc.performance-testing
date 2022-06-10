package ch.resrc.testing.test_capabilities.habits.assertions;

import org.hamcrest.*;

import java.util.List;
import java.util.function.*;

import static ch.resrc.testing.capabilities.functional.PersistentCollections.addedTo;
import static org.hamcrest.Matchers.allOf;

public abstract class FluentMatcher<T, M extends FluentMatcher<T, M>> extends TypeSafeDiagnosingMatcher<T> {

    private final Class<T> type;
    private final Supplier<M> newMatcher;

    private List<Matcher<? super T>> matchers;

    protected FluentMatcher(Class<T> type, Supplier<M> newMatcher) {

        super(type);
        this.type = type;
        this.newMatcher = newMatcher;
        this.matchers = List.of();
    }

    protected FluentMatcher(FluentMatcher<T, M> other) {

        super(other.type);
        this.type = other.type;
        this.newMatcher = other.newMatcher;
        this.matchers = other.matchers;
    }

    protected abstract M copy();

    protected M copied(Consumer<FluentMatcher<T, M>> modification) {

        var copy = copy();
        modification.accept(copy);
        return copy;
    }

    private Matcher<? super T> matcher() {

        return allOf(this.matchers);
    }

    @Override
    protected boolean matchesSafely(T actual, Description mismatchDescription) {

        this.matcher().describeMismatch(actual, mismatchDescription);

        return this.matcher().matches(actual);
    }

    @Override
    public void describeTo(Description description) {

        this.matcher().describeTo(description);
    }

    public M mustMatch(Matcher<? super T> matcher) {

        return this.copied(but -> but.matchers = addedTo(this.matchers, matcher));
    }

    public M not(UnaryOperator<M> op) {

        M toBeNegated = op.apply(newMatcher.get());
        return this.mustMatch(Matchers.not(toBeNegated));
    }

    public M and(Matcher<? super T> matcher) {

        return this.mustMatch(matcher);
    }

}
