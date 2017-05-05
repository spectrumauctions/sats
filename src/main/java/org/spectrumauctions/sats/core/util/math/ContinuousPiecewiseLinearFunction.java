/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.math;

import com.google.common.collect.ImmutableSortedMap;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 *
 */
public final class ContinuousPiecewiseLinearFunction implements Function {

    /**
     * A map containing all linear function pieces of this piecewise linear function.
     * The key is the upper x-value end of this linear function piece.
     */
    private final SortedMap<BigDecimal, LinearFunction> linearFunctions;

    private final BigDecimal lowestX;

    /**
     * Constructs a new PieceWiseLinear function with a restricted domain interval
     * @param cornerPoints A map with <i>key = x-values</i> and <i> value = y-values</i>. 
     * The map has to include all corner points of the function, including lower and upper end of domain. <br>
     *
     */
    public ContinuousPiecewiseLinearFunction(Map<BigDecimal, BigDecimal> cornerPoints) {
        // Sort the incoming corner points by their 
        SortedMap<BigDecimal, BigDecimal> sortedCornerPoints = new TreeMap<>(cornerPoints);
        ImmutableSortedMap.Builder<BigDecimal, LinearFunction> linearFunctionsBuilder =
                ImmutableSortedMap.naturalOrder();
        Iterator<Entry<BigDecimal, BigDecimal>> entryIterator = sortedCornerPoints.entrySet().iterator();
        Entry<BigDecimal, BigDecimal> lowerEntry = entryIterator.next();
        lowestX = lowerEntry.getKey();
        while (entryIterator.hasNext()) {
            Entry<BigDecimal, BigDecimal> upperEntry = entryIterator.next();
            if (lowerEntry.getKey().compareTo(upperEntry.getKey()) == 0) {
                // Skip linear function where the domain is empty
                // TODO log this
            } else {
                LinearFunction linearFunction = new LinearFunction(lowerEntry.getKey(), lowerEntry.getValue(), upperEntry.getKey(), upperEntry.getValue());
                linearFunctionsBuilder.put(upperEntry.getKey(), linearFunction);
                lowerEntry = upperEntry;
            }
        }
        linearFunctions = linearFunctionsBuilder.build();
    }

    /**
     * Provides a list with all corner point (X, Y) pairs, sorted with increasing X.
     * The return type is a list (instead of a sorted map) to allow easy indexing.
     * @return
     */
    public List<SimpleImmutableEntry<BigDecimal, BigDecimal>> getCornerPoints() {
        List<SimpleImmutableEntry<BigDecimal, BigDecimal>> result = new ArrayList<>();
        BigDecimal firstY = getY(lowestX);
        result.add(new SimpleImmutableEntry<>(lowestX, firstY));
        for (BigDecimal functionUpperX : linearFunctions.keySet()) {
            BigDecimal y = getY(functionUpperX);
            result.add(new SimpleImmutableEntry<>(functionUpperX, y));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see Function#getY(java.math.BigDecimal)
     */
    @Override
    public BigDecimal getY(BigDecimal x) {
        try {
            return functionAt(x).getY(x);
        } catch (NotDifferentiableException e) {
            return e.getCornerPointY();
        }
    }


    public LinearFunction functionAt(BigDecimal x) throws NotDifferentiableException {
        if (x.compareTo(lowestX) < 0) {
            throw new OutOfDomainException("X is smaller than domain allows");
        } else if (x.compareTo(lowestX) == 0) {
            try {
                BigDecimal firstKey = linearFunctions.firstKey();
                throw new NotDifferentiableException(null, linearFunctions.get(firstKey), x);
            } catch (NoSuchElementException e) {
                System.out.println("break");
            }

        }
        Iterator<Entry<BigDecimal, LinearFunction>> functionEntries = linearFunctions.entrySet().iterator();
        while (functionEntries.hasNext()) {
            Entry<BigDecimal, LinearFunction> functionEntry = functionEntries.next();
            if (x.compareTo(functionEntry.getKey()) <= 0) {
                LinearFunction higherAdjacentFunction = null;
                if (functionEntries.hasNext()) {
                    higherAdjacentFunction = functionEntries.next().getValue();
                }
                throw new NotDifferentiableException(functionEntry.getValue(), higherAdjacentFunction, x);
            } else if (x.compareTo(functionEntry.getKey()) < 0) {
                return functionEntry.getValue();
            }
        }
        throw new OutOfDomainException("X is bigger than domain allows");
    }


}
