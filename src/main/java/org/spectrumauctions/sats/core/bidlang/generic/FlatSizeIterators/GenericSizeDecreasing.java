/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators;

import com.google.common.collect.Sets;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.BundleEntry;
import org.marketdesignresearch.mechlib.domain.bidder.value.BundleValue;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.*;

/**
 * @author Michael Weiss
 */
public abstract class GenericSizeDecreasing extends GenericSizeOrdered {

    protected GenericSizeDecreasing(Collection<? extends GenericGood> allPossibleGenericDefintions) throws UnsupportedBiddingLanguageException {
        super(allPossibleGenericDefintions);
    }

    @Override
    public Iterator<BundleValue> iterator() {
        return new DecreasingIterator();
    }

    private class DecreasingIterator implements Iterator<BundleValue> {

        int round = 0;
        private Iterator<Set<GenericGood>> definitionPowersetIterator;
        private Map<GenericGood, Integer> roundSize;

        private boolean hasNext;

        protected DecreasingIterator() {
            initNextRound();
        }

        private void initNextRound() {
            roundSize = new HashMap<>();
            for (GenericGood good : allGoods) {
                int quantity = good.available() - round;
                if (quantity < 0) {
                    quantity = 0;
                }
                roundSize.put(good, quantity);
            }
            round++;
            initPowersetIterator();
        }

        private void initPowersetIterator() {
            // Create set of definition with leftover quantities
            Set<GenericGood> leftOverQuantities = new HashSet<>();
            for (GenericGood good : allGoods) {
                if (roundSize.get(good) > 0) {
                    leftOverQuantities.add(good);
                }
            }
            if (leftOverQuantities.size() == 0) {
                hasNext = false;
                return;
            } else {
                hasNext = true;
                Set<Set<GenericGood>> definitionPowerset = Sets.powerSet(leftOverQuantities);
                List<Set<GenericGood>> sorted = new ArrayList<>(definitionPowerset);
                sorted.sort(getIncreasingSizeComparator());
                definitionPowersetIterator = sorted.iterator();
            }

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
        public BundleValue next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            HashSet<BundleEntry> bundleEntries = new HashSet<>();
            Set<GenericGood> toSubstract = definitionPowersetIterator.next();
            for (GenericGood good : allGoods) {
                int quantity = roundSize.get(good);
                if (toSubstract.contains(good)) {
                    quantity--;
                }
                if (quantity > 0) {
                    bundleEntries.add(new BundleEntry(good, quantity));
                }
            }
            if (!definitionPowersetIterator.hasNext()) {
                initNextRound();
            }
            Bundle bundle = new Bundle(bundleEntries);
            return new BundleValue(getBidder().calculateValue(bundle), bundle);
        }

    }
}
