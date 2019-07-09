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
public class DecreasingSizeOrderedXOR extends SizeOrderedXOR {

    public DecreasingSizeOrderedXOR(Collection<? extends License> goods, SATSBidder bidder) {
        super(goods, bidder);
    }

    @Override
    public Iterator<BundleValue> iterator() {
        return new DecreasingIterator();
    }

    private class DecreasingIterator implements Iterator<BundleValue> {

        BigInteger minIndex = BigInteger.ZERO;
        BigInteger index = BigInteger.valueOf(2).pow(DecreasingSizeOrderedXOR.this.goods.size()).subtract(BigInteger.ONE);

        @Override
        public boolean hasNext() {
            return index.compareTo(minIndex) > 0;
        }

        @Override
        public BundleValue next() {
            Bundle bundle = DecreasingSizeOrderedXOR.this.getBundle(index);
            index = index.subtract(BigInteger.ONE);
            return new BundleValue(getBidder().calculateValue(bundle), bundle);
        }

    }
}
