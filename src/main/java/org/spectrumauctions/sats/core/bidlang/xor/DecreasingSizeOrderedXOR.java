/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.xor;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Michael Weiss
 *
 */
public class DecreasingSizeOrderedXOR<T extends Good> extends SizeOrderedXOR<T> {

    public DecreasingSizeOrderedXOR(Collection<T> goods, Bidder<T> bidder) {
        super(goods, bidder);
    }


    @Override
    public Iterator<XORValue<T>> iterator() {
        return new DecreasingIterator();
    }

    private class DecreasingIterator implements Iterator<XORValue<T>> {

        BigInteger minIndex = BigInteger.ZERO;
        BigInteger index = BigInteger.valueOf(2).pow(DecreasingSizeOrderedXOR.this.goods.size()).subtract(BigInteger.ONE);

        @Override
        public boolean hasNext() {
            return index.compareTo(minIndex) > 0;
        }

        @Override
        public XORValue<T> next() {
            Bundle<T> bundle = DecreasingSizeOrderedXOR.this.getBundle(index);
            index = index.subtract(BigInteger.ONE);
            return new XORValue<>(bundle, DecreasingSizeOrderedXOR.this.getValue(bundle));
        }

    }
}
