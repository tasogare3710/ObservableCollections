/*
 * Copyright (C) 2006-2007 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package observable;

import static java.util.Objects.requireNonNull;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@code ObservableCollections} provides factory methods for creating
 * observable lists and maps.
 *
 *
 * @author sky
 */
public final class ObservableCollections {
	private static final class Accessor<T> implements Collection<T> {
		private final T[] array;

		private Accessor(T[] array) {
			this.array = array;
		}

		@Override
		public boolean add(T e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public Iterator<T> iterator() {
			return new Iterator<>() {
				private int length = array.length;
				private int index = 0;

				@Override
				public boolean hasNext() {
					return index < length;
				}

				@Override
				public T next() {
					return array[index++];
				}
			};
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return array.length;
		}

		@Override
		public Object[] toArray() {
			return array;
		}

		@SuppressWarnings({ "unchecked", "hiding" })
		@Override
		public <T> T[] toArray(T[] a) {
			return (T[]) array;
		}
	}

	/**
	 * {@code ObservableListHelper} is created by {@code observableListHelper}, and
	 * useful when changes to individual elements of the list can be tracked.
	 *
	 * @see #observableListHelper
	 */
	public static final class ObservableListHelper<E> {
		private final ObservableListImpl<E> list;

		ObservableListHelper(ObservableListImpl<E> list) {
			this.list = list;
		}

		/**
		 * Sends notification that the element at the specified index has changed.
		 *
		 * @param index the index of the element that has changed
		 * @throws ArrayIndexOutOfBoundsException if index is outside the range of the
		 *                                        {@code List} ({@code < 0 || >= size})
		 */
		public void fireElementChanged(int index) {
			if (index < 0 || index >= list.size()) {
				throw new ArrayIndexOutOfBoundsException("Illegal index");
			}
			list.fireElementChanged(index);
		}

		/**
		 * Returns the {@code ObservableList}.
		 *
		 * @return the observable list
		 */
		public ObservableList<E> getObservableList() {
			return list;
		}
	}

	private static final class ObservableListImpl<E> extends AbstractList<E> implements ObservableList<E> {
		private final boolean supportsElementPropertyChanged;
		private List<E> list;
		private List<ObservableListListener<? super E>> listeners;

		ObservableListImpl(List<E> list, boolean supportsElementPropertyChanged) {
			this.list = list;
			listeners = new CopyOnWriteArrayList<>();
			this.supportsElementPropertyChanged = supportsElementPropertyChanged;
		}

		@Override
		public void add(int index, E element) {
			list.add(index, element);
			modCount++;
			for (var listener : listeners) {
				listener.listElementsAdded(this, index, 1);
			}
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			return addAll(size(), c);
		}

		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			if (list.addAll(index, c)) {
				modCount++;
				for (var listener : listeners) {
					listener.listElementsAdded(this, index, c.size());
				}
			}
			return false;
		}

		@Override
		public void addObservableListListener(ObservableListListener<? super E> listener) {
			listeners.add(listener);
		}

		@Override
		public void clear() {
			var dup = new ArrayList<>(list);
			list.clear();
			modCount++;
			if (dup.size() != 0) {
				for (var listener : listeners) {
					listener.listElementsRemoved(this, 0, dup);
				}
			}
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return list.containsAll(c);
		}

		private void fireElementChanged(int index) {
			for (var listener : listeners) {
				listener.listElementPropertyChanged(this, index);
			}
		}

		@Override
		public E get(int index) {
			return list.get(index);
		}

		@Override
		public E remove(int index) {
			var oldValue = list.remove(index);
			modCount++;
			for (var listener : listeners) {
				listener.listElementsRemoved(this, index, java.util.Collections.singletonList(oldValue));
			}
			return oldValue;
		}

		@Override
		public void removeObservableListListener(ObservableListListener<? super E> listener) {
			listeners.remove(listener);
		}

		@Override
		public E set(int index, E element) {
			var oldValue = list.set(index, element);
			for (var listener : listeners) {
				listener.listElementReplaced(this, index, oldValue);
			}
			return oldValue;
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public boolean supportsElementPropertyChanged() {
			return supportsElementPropertyChanged;
		}

		@Override
		public Object[] toArray() {
			return list.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return list.toArray(a);
		}
	}

	private static final class ObservableMapImpl<K, V> extends AbstractMap<K, V> implements ObservableMap<K, V> {
		private class EntryIterator implements Iterator<Map.Entry<K, V>> {
			private Iterator<Map.Entry<K, V>> realIterator;
			private Map.Entry<K, V> last;

			EntryIterator() {
				realIterator = map.entrySet().iterator();
			}

			@Override
			public boolean hasNext() {
				return realIterator.hasNext();
			}

			@Override
			public Map.Entry<K, V> next() {
				last = realIterator.next();
				return last;
			}

			@Override
			public void remove() {
				if (last == null) {
					throw new IllegalStateException();
				}
				var toRemove = last.getKey();
				last = null;
				ObservableMapImpl.this.remove(toRemove);
			}
		}

		private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
			@Override
			public void clear() {
				ObservableMapImpl.this.clear();
			}

			@Override
			public boolean contains(Object o) {
				if (o instanceof Map.Entry<?, ?> e) {
					return containsKey(e.getKey());
				} else {
					return false;
				}
			}

			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return new EntryIterator();
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof Map.Entry<?, ?> e) {
					var key = e.getKey();
					if (containsKey(key)) {
						remove(key);
						return true;
					}
				}
				return false;
			}

			@Override
			public int size() {
				return ObservableMapImpl.this.size();
			}
		}

		private Map<K, V> map;

		private List<ObservableMapListener<? super K, ? super V>> listeners;

		private Set<Map.Entry<K, V>> entrySet;

		ObservableMapImpl(Map<K, V> map) {
			this.map = map;
			listeners = new CopyOnWriteArrayList<>();
		}

		@Override
		public void addObservableMapListener(ObservableMapListener<? super K, ? super V> listener) {
			listeners.add(listener);
		}

		@Override
		public void clear() {
			// Remove all elements via iterator to trigger notification
			var iterator = keySet().iterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}

		@Override
		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return map.containsValue(value);
		}

		@Override
		public Set<Map.Entry<K, V>> entrySet() {
			if (entrySet == null) {
				entrySet = new EntrySet();
			}
			return entrySet;
		}

		@Override
		public V get(Object key) {
			return map.get(key);
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public V put(K key, V value) {
			V lastValue;
			if (containsKey(key)) {
				lastValue = map.put(key, value);
				for (var listener : listeners) {
					listener.mapKeyValueChanged(this, key, lastValue);
				}
			} else {
				lastValue = map.put(key, value);
				for (var listener : listeners) {
					listener.mapKeyAdded(this, key);
				}
			}
			return lastValue;
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			for (var key : m.keySet()) {
				put(key, m.get(key));
			}
		}

		@Override
		public V remove(Object key) {
			if (containsKey(key)) {
				var value = map.remove(key);
				for (var listener : listeners) {
					listener.mapKeyRemoved(this, key, value);
				}
				return value;
			}
			return null;
		}

		@Override
		public void removeObservableMapListener(ObservableMapListener<? super K, ? super V> listener) {
			listeners.remove(listener);
		}

		@Override
		public int size() {
			return map.size();
		}
	}

	private static final class ObservableSetImpl<E> extends AbstractSet<E> implements ObservableSet<E> {
		private class SetIterator implements Iterator<E> {
			private Iterator<E> realIterator;
			private E last;

			SetIterator() {
				realIterator = set.iterator();
			}

			@Override
			public boolean hasNext() {
				return realIterator.hasNext();
			}

			@Override
			public E next() {
				last = realIterator.next();
				return last;
			}

			@Override
			public void remove() {
				if (last == null) {
					throw new IllegalStateException();
				}
				var toRemove = last;
				last = null;
				ObservableSetImpl.this.remove(toRemove);
			}
		}

		private Set<E> set;
		private List<ObservableSetListener<? super E>> listeners;

		ObservableSetImpl(Set<E> set) {
			this.set = set;
			listeners = new CopyOnWriteArrayList<>();
		}

		@Override
		public boolean add(E e) {
			boolean result = set.add(e);
			if (result) {
				for (var listener : listeners) {
					listener.setElementAdded(this, e);
				}
			}
			return result;
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			boolean modified = false;
			for (var e : c) {
				modified |= add(e);
			}
			return modified;
		}

		@Override
		public void addObservableSetListener(ObservableSetListener<? super E> listener) {
			listeners.add(listener);
		}

		@Override
		public void clear() {
			var iterator = iterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}

		@Override
		public boolean contains(Object o) {
			return set.contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return set.containsAll(c);
		}

		@Override
		public boolean isEmpty() {
			return set.isEmpty();
		}

		@Override
		public Iterator<E> iterator() {
			return new SetIterator();
		}

		@Override
		public boolean remove(Object o) {
			boolean result = set.remove(o);
			if (result) {
				for (var listener : listeners) {
					listener.setElementRemoved(this, o);
				}
			}
			return result;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean modified = false;
			for (var e : c) {
				modified |= remove(e);
			}
			return modified;
		}

		@Override
		public void removeObservableSetListener(ObservableSetListener<? super E> listener) {
			listeners.remove(listener);
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public Object[] toArray() {
			return set.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return set.toArray(a);
		}
	}

	public static <E> ObservableList<E> concat(ObservableList<E>... lists) {
		if (lists.length == 0) {
			return observableArrayList();
		}

		if (lists.length == 1) {
			return observableList(lists[0]);
		}

		var backingList = new ArrayList<E>();
		for (var s : lists) {
			backingList.addAll(s);
		}
		return observableList(backingList);
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

	public static <T> void fill(ObservableList<? super T> list, T obj) {
		@SuppressWarnings("unchecked")
		var newContent = (T[]) new Object[list.size()];
		Arrays.fill(newContent, obj);
		list.clear();
		list.addAll(new Accessor<>(newContent));
	}

	private static <E> ObservableList<E> observableArrayList() {
		return observableList(new ArrayList<>());
	}

	/**
	 * <p>
	 * Creates and returns an {@link ObservableList} wrapping the supplied
	 * {@link List}.
	 *
	 * @param <E>  list element type
	 * @param list the {@code List} to wrap
	 * @return an {@linkplain ObservableList}
	 * @throws NullPointerException if {@code list} is {@code null}
	 */
	public static <E> ObservableList<E> observableList(List<E> list) {
		requireNonNull(list);
		return new ObservableListImpl<>(list, false);
	}

	/**
	 * <p>
	 * Creates and returns an {@link ObservableListHelper} wrapping the supplied
	 * {@link List}. If you can track changes to the underlying list, use this
	 * method instead of {@link #observableList()}.
	 *
	 * @param <E>  list element type
	 * @param list the {@linkplain List} to wrap
	 * @return an {@linkplain ObservableList}
	 * @throws IllegalArgumentException if {@code list} is {@code null}
	 *
	 * @see #observableList(List)
	 */
	public static <E> ObservableListHelper<E> observableListHelper(List<E> list) {
		var oList = new ObservableListImpl<>(list, true);
		return new ObservableListHelper<>(oList);
	}

	/**
	 * <p>
	 * Creates and returns an {@link ObservableMap} wrapping the supplied
	 * {@link Map}.
	 *
	 * @param <K> map key type
	 * @param <V> map value type
	 * @param map the {@linkplain Map} to wrap
	 * @return an {@linkplain ObservableMap}
	 * @throws NullPointerException if {@code map} is {@code map}
	 */
	public static <K, V> ObservableMap<K, V> observableMap(Map<K, V> map) {
		requireNonNull(map);
		return new ObservableMapImpl<>(map);
	}

	/**
	 * <p>
	 * Creates and returns an {@link ObservableSet} wrapping the supplied
	 * {@link Set}.
	 *
	 * @param <E> set element type.
	 * @param set the {@linkplain Set} to wrap
	 * @return an {@linkplain ObservableSet}
	 * @throws NullPointerException if {@code set} is {@code null}
	 */
	public static <E> ObservableSet<E> observableMap(Set<E> set) {
		requireNonNull(set);
		return new ObservableSetImpl<>(set);
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

	public static <T extends Comparable<? super T>> void sort(ObservableList<T> list) {
		sort(list, Comparator.naturalOrder());
	}

	public static <T> void sort(ObservableList<T> list, Comparator<? super T> comparator) {
		var newContent = new ArrayList<>(list);
		newContent.sort(comparator);
		list.clear();
		list.addAll(newContent);
	}
}
