/*
 * Copyright (C) 2006-2007 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package observable;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
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
    /**
     * Creates and returns an {@code ObservableMap} wrapping the supplied
     * {@code Map}.
     *
     * @param map the {@code Map} to wrap
     * @return an {@code ObservableMap}
     * @throws IllegalArgumentException if {@code map} is {@code null}
     */
    public static <K,V> ObservableMap<K,V> observableMap(Map<K,V> map) {
        if (map == null) {
            throw new IllegalArgumentException("Map must be non-null");
        }
        return new ObservableMapImpl<K,V>(map);
    }

    /**
     * Creates and returns an {@code ObservableList} wrapping the supplied
     * {@code List}.
     *
     * @param list the {@code List} to wrap
     * @return an {@code ObservableList}
     * @throws IllegalArgumentException if {@code list} is {@code null}
     */
    public static <E> ObservableList<E> observableList(List<E> list) {
        if (list == null) {
            throw new IllegalArgumentException("List must be non-null");
        }
        return new ObservableListImpl<E>(list, false);
    }

    /**
     * Creates and returns an {@code ObservableListHelper} wrapping
     * the supplied {@code List}. If you can track changes to the underlying
     * list, use this method instead of {@code observableList()}.
     *
     * @param list the {@code List} to wrap
     * @return an {@code ObservableList}
     * @throws IllegalArgumentException if {@code list} is {@code null}
     *
     * @see #observableList
     */
    public static <E> ObservableListHelper<E> observableListHelper(List<E> list) {
        var oList = new ObservableListImpl<E>(list, true);
        return new ObservableListHelper<E>(oList);
    }
    

    /**
     * {@code ObservableListHelper} is created by {@code observableListHelper},
     * and useful when changes to individual elements of the list can be
     * tracked.
     *
     * @see #observableListHelper
     */
    public static final class ObservableListHelper<E> {
        private final ObservableListImpl<E> list;

        ObservableListHelper(ObservableListImpl<E> list) {
            this.list = list;
        }

        /**
         * Returns the {@code ObservableList}.
         *
         * @return the observable list
         */
        public ObservableList<E> getObservableList() {
            return list;
        }

        /**
         * Sends notification that the element at the specified index
         * has changed.
         *
         * @param index the index of the element that has changed
         * @throws ArrayIndexOutOfBoundsException if index is outside the
         *         range of the {@code List} ({@code < 0 || >= size})
         */
        public void fireElementChanged(int index) {
            if (index < 0 || index >= list.size()) {
                throw new ArrayIndexOutOfBoundsException("Illegal index");
            }
            list.fireElementChanged(index);
        }
    }

    private static final class ObservableMapImpl<K,V> extends AbstractMap<K,V> 
            implements ObservableMap<K,V> {
        private Map<K,V> map;
        private List<ObservableMapListener<? super K,? super V>> listeners;
        private Set<Map.Entry<K,V>> entrySet;
        
        ObservableMapImpl(Map<K,V> map) {
            this.map = map;
            listeners = new CopyOnWriteArrayList<ObservableMapListener<? super K,? super V>>();
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
        public Set<Map.Entry<K,V>> entrySet() {
        	if(entrySet == null) {
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
        public int size() {
            return map.size();
        }
        
		@Override
		public void addObservableMapListener(ObservableMapListener<? super K, ? super V> listener) {
			listeners.add(listener);
		}

		@Override
		public void removeObservableMapListener(ObservableMapListener<? super K, ? super V> listener) {
			listeners.remove(listener);
		}

        private class EntryIterator implements Iterator<Map.Entry<K,V>> {
            private Iterator<Map.Entry<K,V>> realIterator;
            private Map.Entry<K,V> last;
            
            EntryIterator() {
                realIterator = map.entrySet().iterator();
            }

            @Override
            public boolean hasNext() {
                return realIterator.hasNext();
            }

            @Override
            public Map.Entry<K,V> next() {
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

        
        private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
            @Override
            public Iterator<Map.Entry<K,V>> iterator() {
                return new EntryIterator();
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

            @Override
            public void clear() {
                ObservableMapImpl.this.clear();
            }
        }
    }

    private static final class ObservableListImpl<E> extends AbstractList<E>
            implements ObservableList<E> {
        private final boolean supportsElementPropertyChanged;
        private List<E> list;
        private List<ObservableListListener<? super E>> listeners;
        
        ObservableListImpl(List<E> list, boolean supportsElementPropertyChanged) {
            this.list = list;
            listeners = new CopyOnWriteArrayList<ObservableListListener<? super E>>();
            this.supportsElementPropertyChanged = supportsElementPropertyChanged;
        }

        @Override
        public E get(int index) {
            return list.get(index);
        }

        @Override
        public int size() {
            return list.size();
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
        public void add(int index, E element) {
            list.add(index, element);
            modCount++;
            for (var listener : listeners) {
                listener.listElementsAdded(this, index, 1);
            }
        }

        @Override
        public E remove(int index) {
            var oldValue = list.remove(index);
            modCount++;
            for (var listener : listeners) {
                listener.listElementsRemoved(this, index,
                        java.util.Collections.singletonList(oldValue));
            }
            return oldValue;
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
        public void clear() {
            var dup = new ArrayList<E>(list);
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

        @Override
        public <T> T[] toArray(T[] a) {
            return list.toArray(a);
        }

        @Override
        public Object[] toArray() {
            return list.toArray();
        }

        private void fireElementChanged(int index) {
            for (var listener : listeners) {
                listener.listElementPropertyChanged(this, index);
            }
        }

        @Override
        public void addObservableListListener(ObservableListListener<? super E> listener) {
        	listeners.add(listener);
        }

        @Override
        public void removeObservableListListener(ObservableListListener<? super E> listener) {
        	listeners.remove(listener);
        }

        @Override
        public boolean supportsElementPropertyChanged() {
            return supportsElementPropertyChanged;
        }
    }
}
