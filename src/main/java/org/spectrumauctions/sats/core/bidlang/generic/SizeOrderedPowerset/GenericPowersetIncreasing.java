/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Iterates over the Powerset of Generic Values.<br>
 * Attention, this is not suitable for very big models, as this implementation will get very very slow for large bundle sizes.
 *
 * @author Michael Weiss
 */
public abstract class GenericPowersetIncreasing<T extends GenericDefinition<S>, S extends Good> extends GenericPowerset<T, S> {

    protected GenericPowersetIncreasing(List<T> genericDefinitions) throws UnsupportedBiddingLanguageException {
        super(genericDefinitions);
    }

    GenericPowersetIncreasing(Map<T, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException {
        super(maxQuantities, maxBundleSize);
    }

    protected void isFeasibleSize(Map<T, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException {
        //Increasing iterator will eventually get very slow, but only after a huge amount of bids were returned
    }

    @Override
    public Iterator<GenericValue<T, S>> iterator() {
        return new IncreasingIterator();
    }

    private class IncreasingIterator extends GenericPowerset<T, S>.PowersetIterator {

        public IncreasingIterator() {
            bundleSize = 1;
            intiPickN();
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return (pickN.hasNext() || bundleSize <= maxBundleSize);
        }


        @Override
        void intiPickN() {
            pickN = new GenericSetsPickN<>(maxQuantities, bundleSize++);
        }

    }
}
