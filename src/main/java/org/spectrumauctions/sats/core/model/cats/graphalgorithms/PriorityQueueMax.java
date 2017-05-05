package org.spectrumauctions.sats.core.model.cats.graphalgorithms;

import java.util.LinkedList;
import java.util.List;

public class PriorityQueueMax<T extends KeyInterface> {

    /*
     * Constructor
     */
    public PriorityQueueMax(int length) {
        _heapLength = length;
        _elements = new LinkedList<>();
        _heapSize = 0;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String str = "Queue (sz=" + _heapSize + "):\n";
        for (T element : _elements)
            str += element.toString() + " ";
        return str;
    }

    public boolean isEmpty() {
        if (_heapSize == 0)
            return true;
        else
            return false;
    }

    /*
     * The method inserts a new element into the queue
     * @param element - the element to be inserted
     */
    public void insert(T element) {
        _heapSize += 1;
        double key = element.getKey();
        element.setKey(-1 * Double.MAX_VALUE);

        _elements.add(element);                        //Add it to the end of the list
        increaseKey(element, _heapSize - 1, key);        //Move it to its place according to its key
    }

    /*
     * The method returns the maximum (top priority according tp the keys) element of the queue
     * @return the maximum element (by key)
     */
    public T max() {
        return _elements.get(0);
    }

    /*
     * The method extracts (i.e. returns and remove from queue) the element with the largest key-value
     * @return the element with the largest key
     */
    public T extractMax() {
        if (_heapSize < 1)
            throw new RuntimeException("PriorityQueueMax queue is empty");

        T max = _elements.get(0);
        _elements.set(0, _elements.get(_heapSize - 1));
        _elements.remove(_heapSize - 1);
        _heapSize--;
        maxHeapify(0);
        return max;
    }

    /*
     * The method increases the key of the element on a given position in the queue
     * @param element - the element which key should be increased
     * @param position - the position of the element in the queue
     * @param key - the key to be assigned to the element
     */
    private void increaseKey(T element, int position, double key) {
        if (key < element.getKey())
            throw new RuntimeException("Wrong key");
        element.setKey(key);

        while ((position > 0) && (_elements.get((int) Math.floor((position - 1) / 2)).getKey() < _elements.get(position).getKey())) {
            int parent = (int) Math.floor((position - 1) / 2);
            T tmp = _elements.get(parent);
            _elements.set(parent, _elements.get(position));
            _elements.set(position, tmp);
            position = parent;
        }

    }

    /*
     * Heapify the heap :-) If the heap property is broken, "repair" the heap
     * @param i - the position from which to heapify
     */
    private void maxHeapify(int i) {
        int l = 2 * (i + 1) - 1;
        int r = 2 * (i + 1);
        int largest = i;

        if ((l <= (_heapSize - 1)) && (_elements.get(l).getKey() > _elements.get(i).getKey()))
            largest = l;
        else
            largest = i;
        if ((r <= (_heapSize - 1)) && (_elements.get(r).getKey() > _elements.get(largest).getKey()))
            largest = r;

        if (largest != i) {
            T tmp = _elements.get(largest);
            _elements.set(largest, _elements.get(i));
            _elements.set(i, tmp);
            maxHeapify(largest);
        }
    }

    private List<T> _elements;                    //The list of elements of the queue
    private final int _heapLength;                //The maximum possible length of queue
    private int _heapSize;                        //The current size of the queue
}
