/*
 * Copyright (C) 2023- tasogare. All rights reserved.
 */
package observable;

/**
 * <p>
 * Notification types from an {@link ObservableSet}
 *
 * @param <E> set element type
 */
public interface ObservableSetListener<E> {
	void setElementAdded(ObservableSet<? extends E> set, Object element);

	void setElementRemoved(ObservableSet<? extends E> set, Object element);
}
