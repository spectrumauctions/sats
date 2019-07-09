/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.xor;

import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.bidder.value.BundleValue;
import org.spectrumauctions.sats.core.model.License;
import org.spectrumauctions.sats.core.model.SATSBidder;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Michael Weiss
 *
 */
public class IncreasingSizeOrderedXOR extends SizeOrderedXOR {

    public IncreasingSizeOrderedXOR(Collection<? extends License> goods, SATSBidder bidder) {
        super(goods, bidder);
    }


    @Override
    public Iterator<BundleValue> iterator() {
        return new IncreasingIterator();
    }

    private class IncreasingIterator implements Iterator<BundleValue> {

        BigInteger index = BigInteger.ONE;
        BigInteger maxIntex = BigInteger.valueOf(2).pow(IncreasingSizeOrderedXOR.this.goods.size());

        @Override
        public boolean hasNext() {
            return index.compareTo(maxIntex) < 0;
        }

        @Override
        public BundleValue next() {
            Bundle bundle = IncreasingSizeOrderedXOR.this.getBundle(index);
            index = index.add(BigInteger.ONE);
            return new BundleValue(getBidder().calculateValue(bundle), bundle);
        }
    }
}
