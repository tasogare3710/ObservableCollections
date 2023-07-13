/*
 * Copyright (C) 2023- tasogare. All rights reserved.
 */
package observable;

import java.util.Set;

/**
 * <p>
 * A {@linkplain Set} that notifies listeners of changes to the {@linkplain Set}.
 *
 * @param <E> set element type
 */
public interface ObservableSet<E> extends Set<E> {
    /**
     * Adds a listener to this observable set.
     *
     * @param listener the listener to add
     */
    public void addObservableSetListener(ObservableSetListener<? super E> listener);

    /**
     * Removes a listener from this observable set.
     *
     * @param listener the listener to remove
     */
    public void removeObservableSetListener(ObservableSetListener<? super E> listener);
}
