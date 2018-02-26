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
public class IncreasingSizeOrderedXOR<T extends Good> extends SizeOrderedXOR<T> {

    public IncreasingSizeOrderedXOR(Collection<T> goods, Bidder<T> bidder) {
        super(goods, bidder);
    }


    @Override
    public Iterator<XORValue<T>> iterator() {
        return new IncreasingIterator();
    }

    private class IncreasingIterator implements Iterator<XORValue<T>> {

        BigInteger index = BigInteger.ONE;
        BigInteger maxIntex = BigInteger.valueOf(2).pow(IncreasingSizeOrderedXOR.this.goods.size());

        @Override
        public boolean hasNext() {
            return index.compareTo(maxIntex) < 0;
        }

        @Override
        public XORValue<T> next() {
            Bundle<T> bundle = IncreasingSizeOrderedXOR.this.getBundle(index);
            index = index.add(BigInteger.ONE);
            return new XORValue<>(bundle, IncreasingSizeOrderedXOR.this.getValue(bundle));
        }
    }
}
