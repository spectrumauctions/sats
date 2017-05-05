/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Weiss
 *
 */
public abstract class GenericPowersetDecreasing<T extends GenericDefinition> extends GenericPowerset<T> {


    GenericPowersetDecreasing(Map<T, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException {
        super(maxQuantities, maxBundleSize);
    }

    protected GenericPowersetDecreasing(List<T> genericDefinitions) throws UnsupportedBiddingLanguageException {
        super(genericDefinitions);
    }

    protected void isFeasibleSize(Map<T, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException {
        if (maxQuantities.size() > 15) {
            //TODO Adjust this limit
            throw new UnsupportedBiddingLanguageException("GenericPowersetDecreasing is not suitable for big model instances");
        }
    }


    /* (non-Javadoc)
     * @see GenericLang#iterator()
     */
    @Override
    public Iterator<GenericValue<T>> iterator() {
        return new DecreasingIterator();
    }


    private class DecreasingIterator extends GenericPowerset<T>.PowersetIterator {

        public DecreasingIterator() {
            bundleSize = maxBundleSize;
            intiPickN();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return (pickN.hasNext() || bundleSize >= 1);
        }


        @Override
        void intiPickN() {
            pickN = new GenericSetsPickN<>(maxQuantities, bundleSize--);
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
