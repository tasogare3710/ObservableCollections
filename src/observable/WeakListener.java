package observable;

import static java.util.Objects.requireNonNull;

import java.lang.ref.WeakReference;

public interface WeakListener {
	public static final class List<E> implements WeakListener, ObservableListListener<E> {
		private final WeakReference<ObservableListListener<E>> ref;

		public List(ObservableListListener<E> listener) {
			requireNonNull(listener);
			ref = new WeakReference<>(listener);
		}

		@Override
		public void listElementPropertyChanged(ObservableList<? extends E> list, int index) {
			var listener = ref.get();
			if (listener != null) {
				listener.listElementPropertyChanged(list, index);
			} else {
				list.removeObservableListListener(this);
			}
		}

		@Override
		public void listElementReplaced(ObservableList<? extends E> list, int index, Object oldElement) {
			var listener = ref.get();
			if (listener != null) {
				listener.listElementReplaced(list, index, oldElement);
			} else {
				list.removeObservableListListener(this);
			}
		}

		@Override
		public void listElementsAdded(ObservableList<? extends E> list, int index, int length) {
			var listener = ref.get();
			if (listener != null) {
				listener.listElementsAdded(list, index, length);
			} else {
				list.removeObservableListListener(this);
			}
		}

		@Override
		public void listElementsRemoved(ObservableList<? extends E> list, int index,
				java.util.List<? extends E> oldElements) {
			var listener = ref.get();
			if (listener != null) {
				listener.listElementsRemoved(list, index, oldElements);
			} else {
				list.removeObservableListListener(this);
			}
		}

		@Override
		public boolean wasGarbageCollected() {
			return ref.get() == null;
		}
	}

	public static final class Map<K, V> implements WeakListener, ObservableMapListener<K, V> {
		private final WeakReference<ObservableMapListener<K, V>> ref;

		public Map(ObservableMapListener<K, V> listener) {
			requireNonNull(listener);
			ref = new WeakReference<>(listener);
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

		@Override
		public boolean wasGarbageCollected() {
			return ref.get() == null;
		}

	}

	public static final class Set<E> implements WeakListener, ObservableSetListener<E> {
		private final WeakReference<ObservableSetListener<E>> ref;

		public Set(ObservableSetListener<E> listener) {
			requireNonNull(listener);
			ref = new WeakReference<>(listener);
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

	boolean wasGarbageCollected();
}
