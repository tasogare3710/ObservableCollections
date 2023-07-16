package observable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class ObservableListImpl<E> extends AbstractList<E> implements ObservableList<E> {
	private final boolean supportsElementPropertyChanged;
	private List<E> list;
	private List<ObservableListListener<? extends E>> listeners;

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
	public void addObservableListListener(ObservableListListener<? extends E> listener) {
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

	void fireElementChanged(int index) {
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
	public void removeObservableListListener(ObservableListListener<? extends E> listener) {
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
	public ObservableList<E> subList(int fromIndex, int toIndex) {
		var sublist = super.subList(fromIndex, toIndex);
		return ObservableCollections.observableList(sublist);
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