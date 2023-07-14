/*
 * Copyright (C) 2006-2007 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * A {@code List} that notifies listeners of changes.
 *
 * @author sky
 */
public interface ObservableList<E> extends List<E> {
	public static <E> ObservableList<E> concat(ObservableList<E>... lists) {
		if (lists.length == 0) {
			return ObservableCollections.observableArrayList();
		}

		if (lists.length == 1) {
			return ObservableCollections.observableList(lists[0]);
		}

		var backingList = new ArrayList<E>();
		for (var s : lists) {
			backingList.addAll(s);
		}
		return ObservableCollections.observableList(backingList);
	}

	public static <T> void copy(ObservableList<? super T> dest, List<? extends T> src) {
		final int srcSize = src.size();
		if (srcSize > dest.size()) {
			throw new IndexOutOfBoundsException("Source does not fit in dest");
		}

		@SuppressWarnings("unchecked")
		var destArray = (T[]) dest.toArray();
		System.arraycopy(src.toArray(), 0, destArray, 0, srcSize);
		dest.addAll(new Accessor<>(destArray));
	}

	/**
	 * <p>
	 * Remove consecutive duplicates using {@link Comparable}.
	 *
	 * <p>
	 * Equivalent to the following call.
	 *
	 * <pre>{@code
	 * ObservableList.dedupBy(list, (a, b) -> a.compareTo(b) == 0);
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param <E>  list element type as {@linkplain Comparable}
	 * @param list
	 *
	 * @see #dedupBy(ObservableList, BiFunction)
	 */
	public static <E extends Comparable<E>> void dedup(ObservableList<E> list) {
		dedupBy(list, (a, b) -> a.compareTo(b) == 0);
	}

	/**
	 * <p>
	 * Remove consecutive duplicates that satisfy the {@code predicate}.
	 *
	 * @param <E>       list element type
	 * @param list
	 * @param predicate
	 */
	public static <E> void dedupBy(ObservableList<E> list, BiFunction<? super E, ? super E, Boolean> predicate) {
		final int size = list.size();
		if (size <= 1) {
			return;
		}

		for (int current = size - 1; current != 0; current--) {
			E a = list.get(current);
			E b = list.get(current - 1);
			if (predicate.apply(a, b)) {
				list.remove(current);
			}
		}
	}

	public static <T> void fill(ObservableList<? super T> list, T obj) {
		@SuppressWarnings("unchecked")
		var newContent = (T[]) new Object[list.size()];
		Arrays.fill(newContent, obj);
		list.clear();
		list.addAll(new Accessor<>(newContent));
	}

	public static <T> boolean replaceAll(ObservableList<T> list, T oldVal, T newVal) {
		@SuppressWarnings("unchecked")
		var newContent = (T[]) list.toArray();
		boolean modified = false;
		for (int i = 0; i < newContent.length; ++i) {
			if (newContent[i].equals(oldVal)) {
				newContent[i] = newVal;
				modified = true;
			}
		}
		if (modified) {
			list.clear();
			list.addAll(new Accessor<>(newContent));
		}
		return modified;
	}

	public static <T> void reverse(ObservableList<T> list) {
		@SuppressWarnings("unchecked")
		var newContent = (T[]) list.toArray();
		for (int i = 0; i < newContent.length / 2; ++i) {
			var tmp = newContent[i];
			newContent[i] = newContent[newContent.length - i - 1];
			newContent[newContent.length - i - 1] = tmp;
		}
		list.clear();
		list.addAll(new Accessor<>(newContent));
	}

	public static <T> void rotate(ObservableList<T> list, int distance) {
		@SuppressWarnings("unchecked")
		var newContent = (T[]) list.toArray();

		int size = list.size();
		distance = distance % size;
		if (distance < 0) {
			distance += size;
		}

		if (distance == 0) {
			return;
		}

		for (int cycleStart = 0, nMoved = 0; nMoved != size; cycleStart++) {
			var displaced = newContent[cycleStart];
			T tmp;
			int i = cycleStart;
			do {
				i += distance;
				if (i >= size) {
					i -= size;
				}
				tmp = newContent[i];
				newContent[i] = displaced;
				displaced = tmp;
				nMoved++;
			} while (i != cycleStart);
		}
		list.clear();
		list.addAll(new Accessor<>(newContent));
	}

	public static <E> void shuffle(ObservableList<E> list, RandomGenerator rng) {
		for (int i = list.size(); i > 1; i--) {
			int from = i - 1;
			int to = rng.nextInt(i);
			// from <-> to
			E tmp = list.get(from);
			list.set(from, list.get(to));
			list.set(to, tmp);
		}
	}

	public static <T extends Comparable<? super T>> void sort(ObservableList<T> list) {
		ObservableList.sort(list, Comparator.naturalOrder());
	}

	public static <T> void sort(ObservableList<T> list, Comparator<? super T> comparator) {
		var newContent = new ArrayList<>(list);
		newContent.sort(comparator);
		list.clear();
		list.addAll(newContent);
	}

	/**
	 *
	 * @param <E>
	 * @param supplier
	 * @return
	 */
	public static <E> Supplier<ObservableList<E>> species(Supplier<List<E>> supplier) {
		return () -> ObservableCollections.observableList(supplier.get());
	}

	/**
	 * Adds a listener that is notified when the list changes.
	 *
	 * @param listener the listener to add
	 */
	public void addObservableListListener(ObservableListListener<? super E> listener);

	/**
	 * Removes a listener.
	 *
	 * @param listener the listener to remove
	 */
	public void removeObservableListListener(ObservableListListener<? super E> listener);

	@Override
	public ObservableList<E> subList(int fromIndex, int toIndex);

	/**
	 * Returns {@code true} if this list sends out notification when the properties
	 * of an element change. This method may be used to determine if a listener
	 * needs to be installed on each of the elements of the list.
	 *
	 * @return {@code true} if this list sends out notification when the properties
	 *         of an element change
	 */
	public boolean supportsElementPropertyChanged();
}
