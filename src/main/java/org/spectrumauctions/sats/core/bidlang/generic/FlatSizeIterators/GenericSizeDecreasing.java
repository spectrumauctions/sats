/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators;

import com.google.common.collect.Sets;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public abstract class GenericSizeDecreasing<T extends GenericDefinition> extends GenericSizeOrdered<T> {

    /**
     * @param allPossibleGenericDefintions
     * @throws UnsupportedBiddingLanguageException
     */
    protected GenericSizeDecreasing(Collection<T> allPossibleGenericDefintions) throws UnsupportedBiddingLanguageException {
        super(allPossibleGenericDefintions);
    }

    @Override
    public Iterator<GenericValue<T>> iterator() {
        return new DecreasingIterator();
    }

    private class DecreasingIterator implements Iterator<GenericValue<T>> {

        private Iterator<Set<T>> definitionPowersetIterator;
        int round = 0;
        private Map<T, Integer> roundSize;

        private boolean hasNext;

        protected DecreasingIterator() {
            initNextRound();
        }

        private void initNextRound() {
            roundSize = new HashMap<>();
            for (T def : allDefintions) {
                int quantity = def.numberOfLicenses() - round;
                if (quantity < 0) {
                    quantity = 0;
                }
                roundSize.put(def, quantity);
            }
            round++;
            initPowersetIterator();
        }

        private void initPowersetIterator() {
            // Create set of definition with leftover quantities
            Set<T> leftOverQuantities = new HashSet<>();
            for (T def : allDefintions) {
                if (roundSize.get(def) > 0) {
                    leftOverQuantities.add(def);
                }
            }
            if (leftOverQuantities.size() == 0) {
                hasNext = false;
                return;
            } else {
                hasNext = true;
                Set<Set<T>> definitionPowerset = Sets.powerSet(leftOverQuantities);
                List<Set<T>> sorted = new ArrayList<>(definitionPowerset);
                sorted.sort(getIncreasingSizeComparator());
                definitionPowersetIterator = sorted.iterator();
            }

        }


        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public GenericValue<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            GenericValue.Builder<T> val = new GenericValue.Builder<>(getGenericBidder());
            Set<T> toSubstract = definitionPowersetIterator.next();
            for (T def : allDefintions) {
                int quantity = roundSize.get(def);
                if (toSubstract.contains(def)) {
                    quantity--;
                }
                val.putQuantity(def, quantity);
            }
            if (!definitionPowersetIterator.hasNext()) {
                initNextRound();
            }
            return val.build();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }


}
