/*
 * Copyright (C) 2006-2007 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package observable;

import java.util.EventListener;
import java.util.List;

/**
 * Notification types from an {@code ObservableList}.
 *
 * @author sky
 */
public interface ObservableListListener<E> extends EventListener {
    /**
     * Notification than a property of an element in this list has changed.
     * Not all {@code ObservableLists} support this notification. Only
     * observable lists that return {@code true} from
     * {@code supportsElementPropertyChanged} send this notification.
     *
     * @param list the {@code ObservableList} that has changed
     * @param index the index of the element that changed
     */
    void listElementPropertyChanged(ObservableList<? extends E> list, int index);

    /**
     * Notification that an element has been replaced by another in the list.
     *
     * @param list the {@code ObservableList} that has changed
     * @param index the index of the element that was replaced
     * @param oldElement the element at the index before the change
     */
    void listElementReplaced(ObservableList<? extends E> list, int index,
                                    Object oldElement);

    /**
     * Notification that elements have been added to the list.
     *
     * @param list the {@code ObservableList} that has changed
     * @param index the index the elements were added to
     * @param length the number of elements that were added
     */
    void listElementsAdded(ObservableList<? extends E> list, int index, int length);

    /**
     * Notification that elements have been removed from the list.
     *
     * @param list the {@code ObservableList} that has changed
     * @param index the starting index the elements were removed from
     * @param oldElements a list containing the elements that were removed.
     */
    void listElementsRemoved(ObservableList<? extends E> list, int index,
                                    List<? extends E> oldElements);
}
