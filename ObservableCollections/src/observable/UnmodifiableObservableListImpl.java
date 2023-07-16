/**
 *
 */
package observable;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
final class UnmodifiableObservableListImpl<E> extends AbstractList<E> implements ObservableList<E> {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private class Delegator implements ObservableListListener<E> {
		@Override
		public void listElementPropertyChanged(ObservableList<? super E> list, int index) {
			for (var l : UnmodifiableObservableListImpl.this.listeners) {
				l.listElementPropertyChanged((ObservableList) UnmodifiableObservableListImpl.this, index);
			}
		}

		@Override
		public void listElementReplaced(ObservableList<? super E> list, int index, Object oldElement) {
			for (var l : UnmodifiableObservableListImpl.this.listeners) {
				l.listElementReplaced((ObservableList) UnmodifiableObservableListImpl.this, index, oldElement);
			}
		}

		@Override
		public void listElementsAdded(ObservableList<? super E> list, int index, int length) {
			for (var l : UnmodifiableObservableListImpl.this.listeners) {
				l.listElementsAdded((ObservableList) UnmodifiableObservableListImpl.this, index, length);
			}
		}

		@Override
		public void listElementsRemoved(ObservableList<? super E> list, int index, List<? super E> oldElements) {
			for (var l : UnmodifiableObservableListImpl.this.listeners) {
				l.listElementsRemoved((ObservableList) UnmodifiableObservableListImpl.this, index, (List) oldElements);
			}
		}
	}

	private final ObservableList<E> inner;
	private CopyOnWriteArrayList<WeakListener.List<? super E>> listeners;
	private WeakListener.List<? super E> listener;

	/**
	 * 
	 * @param list inner list
	 */
	@SuppressWarnings("unchecked")
	UnmodifiableObservableListImpl(ObservableList<E> list) {
		this.inner = list;
		listeners = new CopyOnWriteArrayList<>();
		listener = new WeakListener.List<>(new Delegator());
		this.inner.addObservableListListener((ObservableListListener<? extends E>) listener);
	}

	/**
	 * @throw UnsupportedOperationException
	 */
	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throw UnsupportedOperationException
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throw UnsupportedOperationException
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throw UnsupportedOperationException
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addObservableListListener(ObservableListListener<? extends E> listener) {
		listeners.add(new WeakListener.List(listener));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E get(int index) {
		return inner.get(index);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void removeObservableListListener(ObservableListListener<? extends E> listener) {
		if (listener instanceof WeakListener<?> weak) {
			listeners.remove(weak);
		} else {
			// XXX: これでいいか？
			for (var i = 0; i < listeners.size(); i++) {
				var target = listeners.get(i);
				if (target.refersTo((ObservableListListener) listener)) {
					listeners.remove(i);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return inner.size();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return returns an unmodifiable {@link ObservableList}.
	 */
	@Override
	public ObservableList<E> subList(int fromIndex, int toIndex) {
		return new UnmodifiableObservableListImpl<>(inner.subList(fromIndex, toIndex));
	}

	@Override
	public boolean supportsElementPropertyChanged() {
		return false;
	}
}
