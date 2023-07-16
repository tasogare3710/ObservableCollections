package observable;

import static java.util.Objects.requireNonNull;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public interface WeakListener<T> {
	public static final class List<E> implements WeakListener<ObservableListListener<E>>, ObservableListListener<E> {
		private final WeakReference<ObservableListListener<E>> ref;

		public List(ObservableListListener<E> listener) {
			ref = new WeakReference<>(requireNonNull(listener));
		}

		public List(ObservableListListener<E> listener, ReferenceQueue<ObservableListListener<E>> queue) {
			ref = new WeakReference<>(requireNonNull(listener), queue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear() {
			ref.clear();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean enqueue() {
			return ref.enqueue();
		}

		@Override
		public void listElementPropertyChanged(ObservableList<? super E> list, int index) {
			var listener = ref.get();
			if (listener != null) {
				listener.listElementPropertyChanged(list, index);
			} else {
				list.removeObservableListListener(this);
			}
		}

		@Override
		public void listElementReplaced(ObservableList<? super E> list, int index, Object oldElement) {
			var listener = ref.get();
			if (listener != null) {
				listener.listElementReplaced(list, index, oldElement);
			} else {
				list.removeObservableListListener(this);
			}
		}

		@Override
		public void listElementsAdded(ObservableList<? super E> list, int index, int length) {
			var listener = ref.get();
			if (listener != null) {
				listener.listElementsAdded(list, index, length);
			} else {
				list.removeObservableListListener(this);
			}
		}

		@Override
		public void listElementsRemoved(ObservableList<? super E> list, int index,
				java.util.List<? super E> oldElements) {
			var listener = ref.get();
			if (listener != null) {
				listener.listElementsRemoved(list, index, oldElements);
			} else {
				list.removeObservableListListener(this);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final boolean refersTo(ObservableListListener<E> listener) {
			return ref.refersTo(listener);
		}

		@Override
		public boolean wasGarbageCollected() {
			return ref.get() == null;
		}
	}

	public static final class Map<K, V>
			implements WeakListener<ObservableMapListener<K, V>>, ObservableMapListener<K, V> {
		private final WeakReference<ObservableMapListener<K, V>> ref;

		public Map(ObservableMapListener<K, V> listener) {
			ref = new WeakReference<>(requireNonNull(listener));
		}

		public Map(ObservableMapListener<K, V> listener, ReferenceQueue<ObservableMapListener<K, V>> queue) {
			ref = new WeakReference<>(requireNonNull(listener), queue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear() {
			ref.clear();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean enqueue() {
			return ref.enqueue();
		}

		@Override
		public void mapKeyAdded(ObservableMap<? extends K, ? extends V> map, Object key) {
			var listener = ref.get();
			if (listener != null) {
				listener.mapKeyAdded(map, key);
			} else {
				map.removeObservableMapListener(this);
			}
		}

		@Override
		public void mapKeyRemoved(ObservableMap<? extends K, ? extends V> map, Object key, Object value) {
			var listener = ref.get();
			if (listener != null) {
				listener.mapKeyRemoved(map, key, value);
			} else {
				map.removeObservableMapListener(this);
			}
		}

		@Override
		public void mapKeyValueChanged(ObservableMap<? extends K, ? extends V> map, Object key, Object lastValue) {
			var listener = ref.get();
			if (listener != null) {
				listener.mapKeyValueChanged(map, key, lastValue);
			} else {
				map.removeObservableMapListener(this);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final boolean refersTo(ObservableMapListener<K, V> obj) {
			return ref.refersTo(obj);
		}

		@Override
		public boolean wasGarbageCollected() {
			return ref.get() == null;
		}

	}

	public static final class Set<E> implements WeakListener<ObservableSetListener<E>>, ObservableSetListener<E> {
		private final WeakReference<ObservableSetListener<E>> ref;

		public Set(ObservableSetListener<E> listener) {
			ref = new WeakReference<>(requireNonNull(listener));
		}

		public Set(ObservableSetListener<E> listener, ReferenceQueue<ObservableSetListener<E>> queue) {
			ref = new WeakReference<>(requireNonNull(listener), queue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear() {
			ref.clear();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean enqueue() {
			return ref.enqueue();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean refersTo(ObservableSetListener<E> obj) {
			return ref.refersTo(obj);
		}

		@Override
		public void setElementAdded(ObservableSet<? extends E> set, Object element) {
			var listener = ref.get();
			if (listener != null) {
				listener.setElementAdded(set, element);
			} else {
				set.removeObservableSetListener(this);
			}
		}

		@Override
		public void setElementRemoved(ObservableSet<? extends E> set, Object element) {
			var listener = ref.get();
			if (listener != null) {
				listener.setElementRemoved(set, element);
			} else {
				set.removeObservableSetListener(this);
			}
		}

		@Override
		public boolean wasGarbageCollected() {
			return ref.get() == null;
		}
	}

	/**
	 * @see {@link WeakReference#clear()}
	 */
	void clear();

	/**
	 * @return
	 *
	 * @see {@link WeakReference#enqueue()}
	 */
	boolean enqueue();

	/**
	 * @param obj
	 * @return
	 *
	 * @see {@link WeakReference#refersTo(Object))}
	 */
	boolean refersTo(T obj);

	boolean wasGarbageCollected();
}
