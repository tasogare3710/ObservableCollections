/*
 * Copyright (C) 2006-2007 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package observable;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * {@code ObservableCollections} provides factory methods for creating
 * observable lists and maps.
 *
 *
 * @author sky
 */
public final class ObservableCollections {
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

	static <E> ObservableList<E> observableArrayList() {
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

	@SuppressWarnings("unchecked")
	public static <E> Collector<E, ObservableList<E>, ObservableList<E>> toObservableList(Supplier<List<E>> supplier) {
		return Collector.of(ObservableList.species(supplier), List::add, ObservableList::concat);
	}

	/**
	 * <p>
	 * Creates and returns an unmodifiable {@link ObservableList} wrapping the
	 * supplied {@linkplain ObservableList}.
	 *
	 * @param <E>
	 * @param list
	 * @return
	 */
	public static <E> ObservableList<E> unmodifiableObservableList(ObservableList<E> list) {
		return new UnmodifiableObservableListImpl<>(list);
	}
}
