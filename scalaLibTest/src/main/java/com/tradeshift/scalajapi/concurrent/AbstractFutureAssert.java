package com.tradeshift.scalajapi.concurrent;

import static com.tradeshift.scalajapi.concurrent.FutureShouldComplete.shouldCompleteWithin;
import static com.tradeshift.scalajapi.concurrent.FutureShouldFail.shouldFail;
import static com.tradeshift.scalajapi.concurrent.FutureShouldSucceed.shouldSucceed;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.internal.ComparatorBasedComparisonStrategy;
import org.assertj.core.internal.ComparisonStrategy;
import org.assertj.core.internal.FieldByFieldComparator;
import org.assertj.core.internal.StandardComparisonStrategy;

/**
 * Assertions for {@link Future}.
 * 
 * @param <T> type of the value contained in the {@link Future}.  
 */
public class AbstractFutureAssert<S extends AbstractFutureAssert<S, T>, T> extends AbstractAssert<S, Future<T>> {
    private ComparisonStrategy optionalValueComparisonStrategy;
    private Duration timeout;

    protected AbstractFutureAssert(Future<T> actual, Class<?> selfType, Duration timeout) {
      super(actual, selfType);
      this.timeout = timeout;
      this.optionalValueComparisonStrategy = StandardComparisonStrategy.instance();
    }
    
    public S within(long amount, TemporalUnit unit) {
        this.timeout = Duration.of(amount, unit);
        return myself;
    }
    
    public S within(Duration timeout) {
        this.timeout = timeout;
        return myself;
    }
    
    public S completes() {
        isNotNull();
        try {
            Await.result(actual, timeout);
        } catch (TimeoutException x) {
            throwAssertionError(shouldCompleteWithin(actual, timeout));
        } catch (Throwable x) {
            // Any other exception still completes the future, so that's OK.
        }
        return myself;
    }
    
    public S succeeds() {
        isNotNull();
        try {
            Await.result(actual, timeout);
        } catch (TimeoutException x) {
            throwAssertionError(shouldCompleteWithin(actual, timeout));
        } catch (Throwable x) {
            throwAssertionError(shouldSucceed(x));
        }
        return myself;
    }
    
    public AbstractObjectAssert<?, T> success() {
        isNotNull();
        T value = null;
        try {
            value = Await.result(actual, timeout);
        } catch (TimeoutException x) {
            throwAssertionError(shouldCompleteWithin(actual, timeout));
        } catch (Throwable x) {
            throwAssertionError(shouldSucceed(x));
        }
        return Assertions.assertThat(value);
    }
    
    public S succeedsWith(T expectedValue) {
        isNotNull();
        checkNotNull(expectedValue);
        try {
            T value = Await.result(actual, timeout);
            if (!optionalValueComparisonStrategy.areEqual(value, expectedValue)) throwAssertionError(shouldSucceed(value, expectedValue));
        } catch (TimeoutException x) {
            throwAssertionError(shouldCompleteWithin(actual, timeout));
        } catch (AssertionError error) {
            throw error;
        } catch (Throwable x) {
            throwAssertionError(shouldSucceed(x, expectedValue));
        }
        return myself;
    }
    
    public S fails() {
        isNotNull();
        try {
            T value = Await.result(actual, timeout);
            throwAssertionError(shouldFail(value));
        } catch (TimeoutException x) {
            throwAssertionError(shouldCompleteWithin(actual, timeout));
        } catch (AssertionError error) {
            throw error;
        } catch (Throwable x) {
            // Future has failed, so that's OK.
        }
        return myself;
    }

    public AbstractThrowableAssert<?, ? extends Throwable> failure() {
        isNotNull();
        Throwable failure = null;
        try {
            T value = Await.result(actual, timeout);
            throwAssertionError(shouldFail(value));
        } catch (TimeoutException x) {
            throwAssertionError(shouldCompleteWithin(actual, timeout));
        } catch (AssertionError error) {
            throw error;
        } catch (Throwable x) {
            // Future has failed, so that's OK.
            failure = x;
        }
        return Assertions.assertThat(failure);        
    }
    
    /**
     * Use field/property by field/property comparison (including inherited fields/properties) instead of relying on
     * actual type A <code>equals</code> method to compare the {@link Optional} value's object for incoming assertion
     * checks. Private fields are included but this can be disabled using
     * {@link Assertions#setAllowExtractingPrivateFields(boolean)}.
     * 
     * This can be handy if <code>equals</code> method of the {@link Optional} value's object to compare does not suit
     * you.
     * 
     * Note that the comparison is <b>not</b> recursive, if one of the fields/properties is an Object, it will be
     * compared to the other field/property using its <code>equals</code> method.
     * 
     * Example:
     * 
     * <pre><code class='java'> TolkienCharacter frodo = new TolkienCharacter("Frodo", 33, HOBBIT);
     * TolkienCharacter frodoClone = new TolkienCharacter("Frodo", 33, HOBBIT);
     *  
     * // Fail if equals has not been overridden in TolkienCharacter as equals default implementation only compares references
     * assertThat(Optional.of(frodo)).contains(frodoClone);
     *  
     * // frodo and frodoClone are equals when doing a field by field comparison.
     * assertThat(Optional.of(frodo)).usingFieldByFieldValueComparator().contains(frodoClone);</code></pre>
     *
     * @return {@code this} assertion object.
     */
    public S usingFieldByFieldValueComparator() {
      return usingValueComparator(new FieldByFieldComparator());
    }

    /**
     * Use given custom comparator instead of relying on actual type A <code>equals</code> method to compare the
     * {@link Optional} value's object for incoming assertion checks.
     * <p>
     * Custom comparator is bound to assertion instance, meaning that if a new assertion is created, it will use default
     * comparison strategy.
     * <p>
     * Examples :
     *
     * <pre><code class='java'> TolkienCharacter frodo = new TolkienCharacter("Frodo", 33, HOBBIT);
     * TolkienCharacter frodoClone = new TolkienCharacter("Frodo", 33, HOBBIT);
     * 
     * // Fail if equals has not been overridden in TolkienCharacter as equals default implementation only compares references
     * assertThat(Optional.of(frodo)).contains(frodoClone);
     * 
     * // frodo and frodoClone are equals when doing a field by field comparison.
     * assertThat(Optional.of(frodo)).usingValueComparator(new FieldByFieldComparator()).contains(frodoClone);</code></pre>
     *
     * @param customComparator the comparator to use for incoming assertion checks.
     * @throws NullPointerException if the given comparator is {@code null}.
     * @return {@code this} assertion object.
     */
    public S usingValueComparator(Comparator<? super T> customComparator) {
      optionalValueComparisonStrategy = new ComparatorBasedComparisonStrategy(customComparator);
      return myself;
    }

    /**
     * Revert to standard comparison for incoming assertion {@link Optional} value checks.
     * <p>
     * This method should be used to disable a custom comparison strategy set by calling
     * {@link #usingValueComparator(Comparator)}.
     *
     * @return {@code this} assertion object.
     */
    public S usingDefaultValueComparator() {
      // fall back to default strategy to compare actual with other objects.
      optionalValueComparisonStrategy = StandardComparisonStrategy.instance();
      return myself;
    }
    
    private void checkNotNull(T expectedValue) {
        if (expectedValue == null) throw new IllegalArgumentException("The expected value should not be <null>.");
    }    
}
