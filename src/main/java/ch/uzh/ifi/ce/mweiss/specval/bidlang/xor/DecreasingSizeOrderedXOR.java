/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.bidlang.xor;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;

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
        BigInteger index= BigInteger.valueOf(2).pow(DecreasingSizeOrderedXOR.this.goods.size()).subtract(BigInteger.ONE);

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
        
        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
