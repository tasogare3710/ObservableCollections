package observable;

import java.util.List;

/**
 * {@linkplain ObservableListHelper} is created by
 * {@linkplain observableListHelper}, and useful when changes to individual
 * elements of the list can be tracked.
 *
 * @see ObservableCollections#observableListHelper(List)
 */
public final class ObservableListHelper<E> {
	private final ObservableListImpl<E> list;

	ObservableListHelper(ObservableListImpl<E> list) {
		this.list = list;
	}

	/**
	 * Sends notification that the element at the specified index has changed.
	 *
	 * @param index the index of the element that has changed
	 * @throws ArrayIndexOutOfBoundsException if index is outside the range of the
	 *                                        {@linkplain List}
	 *                                        ({@code < 0 || >= size})
	 */
	public void fireElementChanged(int index) {
		if (index < 0 || index >= list.size()) {
			throw new ArrayIndexOutOfBoundsException("Illegal index");
		}
		list.fireElementChanged(index);
	}

	/**
	 *
	 * @return the {@linkplain ObservableList}.
	 */
	public ObservableList<E> getObservableList() {
		return list;
	}
}