package observable;

import java.util.Collection;
import java.util.Iterator;

final class Accessor<T> implements Collection<T> {
	private final T[] array;

	Accessor(T[] array) {
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