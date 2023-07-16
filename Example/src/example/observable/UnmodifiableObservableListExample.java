/**
 *
 */
package example.observable;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import observable.ObservableCollections;
import observable.ObservableList;
import observable.ObservableListListener;

/**
 *
 */
public class UnmodifiableObservableListExample {
	private static final System.Logger LOG = System.getLogger(UnmodifiableObservableListExample.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new UnmodifiableObservableListExample(args);
	}

	/**
	 *
	 */
	public UnmodifiableObservableListExample(String[] args) {
		// TODO Auto-generated constructor stub
		var observable = IntStream.range(0, 16).map(x -> x * x).collect(ObservableList.<Integer>species(ArrayList::new),
				List::add, List::addAll);
		var unmodifiable = ObservableCollections.unmodifiableObservableList(observable);
		var listener = new ObservableListListener<Integer>() {
			@Override
			public void listElementPropertyChanged(ObservableList<? super Integer> list, int index) {
				LOG.log(Level.INFO, "ElementPropertyChanged: %1$s".formatted(list.get(index)));
			}

			@Override
			public void listElementReplaced(ObservableList<? super Integer> list, int index, Object oldElement) {
				LOG.log(Level.INFO, "ElementReplaced: %1$s to %2$s".formatted(oldElement, list.get(index)));
			}

			@Override
			public void listElementsAdded(ObservableList<? super Integer> list, int index, int length) {
				var sublist = list.subList(index, index + length);
				LOG.log(Level.INFO, "ElementsAdded: %1$s".formatted(sublist));
			}

			@Override
			public void listElementsRemoved(ObservableList<? super Integer> list, int index,
					List<? super Integer> oldElements) {
				LOG.log(Level.INFO, "ElementsRemoved: %1$s".formatted(oldElements));
			}
		};
		unmodifiable.addObservableListListener(new ObservableListListener<Integer>() {
			@Override
			public void listElementPropertyChanged(ObservableList<? super Integer> list, int index) {
				LOG.log(Level.INFO, "listener2: %1$s".formatted(list.get(index)));
			}

			@Override
			public void listElementReplaced(ObservableList<? super Integer> list, int index, Object oldElement) {
				LOG.log(Level.INFO, "listener2: %1$s to %2$s".formatted(oldElement, list.get(index)));
			}

			@Override
			public void listElementsAdded(ObservableList<? super Integer> list, int index, int length) {
				var sublist = list.subList(index, index + length);
				LOG.log(Level.INFO, "listener2: %1$s".formatted(sublist));
			}

			@Override
			public void listElementsRemoved(ObservableList<? super Integer> list, int index,
					List<? super Integer> oldElements) {
				LOG.log(Level.INFO, "listener2: %1$s".formatted(oldElements));
			}
		});
		unmodifiable.addObservableListListener(listener);
		observable.add(17);
		observable.remove(5);
		observable.set(2, 100);

		unmodifiable.removeObservableListListener(listener);
		observable.add(17);
		observable.remove(5);
		observable.set(2, 100);
	}
}
