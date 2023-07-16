/**
 *
 */
package example.observable;

import static java.lang.System.nanoTime;

import java.lang.System.Logger.Level;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import observable.ObservableCollections;
import observable.ObservableList;
import observable.ObservableListListener;
import observable.WeakListener;

/**
 *
 */
public class ObservableListExample {
	private static final System.Logger LOG = System.getLogger(ObservableListExample.class.getName());

	public static byte[] longsToBytes(long... array) {
		byte[] result = new byte[Long.BYTES * array.length];
		for (int i = array.length - 1; i >= 0; i--) {
			var e = array[i];
			result[i * Long.BYTES + 7] = (byte) ((e >>> 56) & 0xFF);
			result[i * Long.BYTES + 6] = (byte) ((e >>> 48) & 0xFF);
			result[i * Long.BYTES + 5] = (byte) ((e >>> 40) & 0xFF);
			result[i * Long.BYTES + 4] = (byte) ((e >>> 32) & 0xFF);
			result[i * Long.BYTES + 3] = (byte) ((e >>> 24) & 0xFF);
			result[i * Long.BYTES + 2] = (byte) ((e >>> 16) & 0xFF);
			result[i * Long.BYTES + 1] = (byte) ((e >>> 8) & 0xFF);
			result[i * Long.BYTES + 0] = (byte) ((e >>> 0) & 0xFF);
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ObservableListExample();
	}

	private ObservableListListener<Integer> listener;

	ObservableListExample() {
		var a = IntStream.range(0, 16).map(x -> x * x).collect(ObservableList.<Integer>species(ArrayList::new),
				List::add, List::addAll);
		listener = new ObservableListListener<>() {
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
		a.addObservableListListener(listener);

		LOG.log(Level.INFO, "replaceAll");
		ObservableList.replaceAll(a, 0, 100);
		LOG.log(Level.INFO, "rotate");
		ObservableList.rotate(a, 3);
		LOG.log(Level.INFO, "fill");
		ObservableList.fill(a.subList(0, 4), 32);
		LOG.log(Level.INFO, "copy");
		ObservableList.copy(a, List.of(5, 6, 7));
		LOG.log(Level.INFO, "sort");
		ObservableList.sort(a);
		LOG.log(Level.INFO, "reverse");
		ObservableList.reverse(a);
		LOG.log(Level.INFO, "dedup");
		ObservableList.dedup(a);

		var b = Stream.of(200, 300, 777).collect(ObservableCollections.toObservableList(ArrayList::new));
		LOG.log(Level.INFO, "concat");
		@SuppressWarnings("unchecked")
		var c = ObservableList.<Integer>concat(a, b);
		var weakListener = new WeakListener.List<>(listener);
		c.addObservableListListener(weakListener);

		var seed0 = longsToBytes(nanoTime(), nanoTime(), nanoTime(), nanoTime());
		// L64X1024MixRandomに必要なseedはlong[18]
		var seed1 = new SecureRandom(seed0).generateSeed(Long.BYTES * 18);
		var rng = RandomGeneratorFactory.of("L64X1024MixRandom").create(seed1);
		LOG.log(Level.INFO, "shuffle");
		ObservableList.shuffle(c, rng);
		LOG.log(Level.INFO, c);

		// 弱参照を外すと回収対象になるのでcのリスナーは呼ばれない。
		weakListener.clear();
		ObservableList.shuffle(c, rng);
		LOG.log(Level.INFO, c);
	}
}
