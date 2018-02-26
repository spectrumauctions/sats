/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 */
public final class GenericSetsPickN<T> implements Iterator<Map<T, Integer>> {

    private final ImmutableList<T> quantifiableObjects;
    private final int target;
    private final ColumnWalker firstWalker;

    private Map<T, Integer> next;
    private boolean hasNext;

    /**
     * @param maxQuantities The maximum quantities per type to be returned. The iterator of this map has to return the keys in increasing order of priority
     * @param target        how many items should be picked in total
     */
    public GenericSetsPickN(Map<T, Integer> maxQuantities, int target) {
        Preconditions.checkArgument(target > 0);
        this.target = target;
        // Init quantifiableObjects
        List<T> quantifiableObjectsIncreasingPriority = new ArrayList<>(maxQuantities.keySet());
        Collections.reverse(quantifiableObjectsIncreasingPriority);
        quantifiableObjects = ImmutableList.copyOf(quantifiableObjectsIncreasingPriority);
        // Initiate Walkers, starting with the last one
        Preconditions.checkArgument(maxQuantities.size() > 0);
        ColumnWalker previouslyCreatedWalker = null;
        for (Entry<T, Integer> maxQuantity : maxQuantities.entrySet()) {
            previouslyCreatedWalker = new ColumnWalker(previouslyCreatedWalker, maxQuantity.getValue());
        }
        firstWalker = previouslyCreatedWalker;
        hasNext = true;
        next();
    }

    /**
     * Appends 0 to a list until it has a certain size
     */
    private static void appendZeros(int size, List<Integer> list) {
        while (list.size() < size) {
            list.add(0);
        }
    }

    private static int sum(List<Integer> summands) {
        int result = 0;
        for (int summand : summands) {
            result += summand;
        }
        return result;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public Map<T, Integer> next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        Map<T, Integer> toReturn = next;
        prepareNext();
        return toReturn;
    }

    /**
     * Method to generate the next return value and set hasNext appropriately.
     */
    private void prepareNext() {
        List<Integer> nextList = firstWalker.walkToNextResult(new ArrayList<>());
        if (nextList == null) {
            hasNext = false;
            return;
        }
        appendZeros(quantifiableObjects.size(), nextList);
        next = new HashMap<>();
        for (int i = 0; i < quantifiableObjects.size(); i++) {
            T object = quantifiableObjects.get(i);
            Integer quantity = nextList.get(i);
            next.put(object, quantity);
        }
    }

    private class ColumnWalker {
        private final ColumnWalker nextWalker;
        private final int maxValue;
        private int current;

        /**
         * @param nextWalker The walker (i.e. number generator) for the next less priorized quantifiable object
         * @param maxValue   the maximum quantity the quantifiable object this walker is working for can have.
         */
        private ColumnWalker(ColumnWalker nextWalker, int maxValue) {
            super();
            this.nextWalker = nextWalker;
            this.maxValue = maxValue;
            this.current = maxValue;
        }

        /**
         * @return the next combination of values that sum up to the targetValue.
         */
        private List<Integer> walkToNextResult(List<Integer> previousSteps) {
            // Check if this is the last walker without any leftover options
            if (nextWalker == null && current == 0) {
                return null;
            }

            int sum = previousSteps.stream().mapToInt(Integer::intValue).sum() + current;
            if (sum > target) {
                if (current == 0) {
                    return null;
                }
                // There is no possible solution with the actual "current", try next closer "current"
                this.oneStepForward();
                List<Integer> downlinkResult = this.walkToNextResult(previousSteps);
                if (downlinkResult != null) {
                    return downlinkResult;
                } else { // downlinkresult == null and current > 0
                    return this.walkToNextResult(previousSteps);
                }
            } else if (sum == target) {
                previousSteps.add(current);
                this.oneStepForward();
                return previousSteps;
            } else { //sum < target
                previousSteps.add(current);
                List<Integer> nextWalkerResult;
                if (nextWalker == null) {
                    nextWalkerResult = null;
                } else {
                    nextWalkerResult = nextWalker.walkToNextResult(previousSteps);
                }
                if (nextWalkerResult == null) {
                    previousSteps.remove(previousSteps.size() - 1);
                    if (current == 0) {
                        return null;
                    } else {
                        this.oneStepForward();
                        return this.walkToNextResult(previousSteps);
                    }
                } else {
                    return nextWalkerResult;
                }
            }
        }

        /**
         * Set the current walker on step forward, <br>
         * i.e., decrease its current quantity by one and put the quantities of all subsequent walkers to their max value
         */
        private void oneStepForward() {
            current--;
            if (nextWalker != null) {
                nextWalker.reset();
            }
        }

        /**
         * Put a walker, and all subsequent walkers to their max value.
         */
        private void reset() {
            current = maxValue;
            if (nextWalker != null) {
                nextWalker.reset();
            }
        }
    }

}
