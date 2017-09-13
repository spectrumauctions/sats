/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.xor;

import com.google.common.math.BigIntegerMath;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SizeOrderedXOR<T extends Good> implements XORLanguage<T> {

    final List<T> goods = new ArrayList<>();
    private Bidder<T> bidder;

    protected SizeOrderedXOR(Collection<T> goods, Bidder<T> bidder) {
        this.goods.addAll(goods);
        this.bidder = bidder;
    }

    /* (non-Javadoc)
     * @see BiddingLanguage#getBidder()
     */
    @Override
    public Bidder<T> getBidder() {
        return bidder;
    }

    /**
     * Get the bundle with index index
     * @param index
     * @return
     */
    public Bundle<T> getBundle(BigInteger index) {
        String bundleRepresentation = packageRepresentation(index, goods.size()).toString();
        return getBundle(bundleRepresentation);
    }

    private Bundle<T> getBundle(String bundleRepresentation) {
        Bundle<T> result = new Bundle<>();
        for (int i = 0; i < bundleRepresentation.length(); i++) {
            if (bundleRepresentation.charAt(i) == '1') {
                result.add(goods.get(i));
            }
        }
        return result;
    }

    protected BigDecimal getValue(Bundle bundle) {
        return getBidder().calculateValue(bundle);
    }

    /**
     * Returns a specific bundle of given size
     *
     * @param subIndex
     *            : An index of this bundle in a list of all bundles with same size (hence NOT the index in the iterator).
     *            (starting a zero).
     * @param size the size of the bundle
     * @return
     */
    public Bundle<T> getBundle(BigInteger subIndex, int size) {
        // TODO check if subIndex is valid;
        String binaryString = recBinaryString(subIndex, goods.size(), size).toString();
        return getBundle(binaryString);
    }

    /**
     * The String representation of the bundle, i.e., 1/0 for all licenses
     * @param index
     * @param n
     * @return
     */
    public static StringBuilder packageRepresentation(BigInteger index, int n) {
        SizeStarter foundSize = bundleSize(index, n);
        BigInteger sizeBasedIndex = index.subtract(foundSize.sizeStart);
        return recBinaryString(sizeBasedIndex, n, foundSize.size);
    }

    private static SizeStarter bundleSize(BigInteger index, int n) {
        BigInteger sum = BigInteger.ZERO;
        BigInteger previousSum = null;
        int size = 0;
        while (sum.compareTo(index) < 0) {
            size++;
            if (size > n) {
                throw new RuntimeException("Index to big for available number of items: index=" + index.toString());
            }
            BigInteger thisSizeBundles = BigIntegerMath.binomial(n, size);
            previousSum = sum;
            sum = sum.add(thisSizeBundles);
        }
        return new SizeStarter(size, previousSum);
    }

    private static class SizeStarter {
        private final BigInteger sizeStart;
        private final int size;

        public SizeStarter(int size, BigInteger sizeStart) {
            this.sizeStart = sizeStart;
            this.size = size;
        }
    }

    private static StringBuilder recBinaryString(BigInteger sizeBasedIndex, int n, int k) {

        if (n == 0) {
            return new StringBuilder();
        }
        if (k == 0) {
            return new StringBuilder("0").append(recBinaryString(sizeBasedIndex, n - 1, 0));
        }

        // Compute share starting with a one
        BigInteger bin = BigIntegerMath.binomial(n, k);
        BigInteger biggestOneStarterIndex = bin.multiply(BigInteger.valueOf(k)).divide(BigInteger.valueOf(n));

        if (sizeBasedIndex.compareTo(biggestOneStarterIndex) <= 0) {
            return new StringBuilder("1").append(recBinaryString(sizeBasedIndex, n - 1, k - 1));
        } else {
            BigInteger newIndex = sizeBasedIndex.subtract(biggestOneStarterIndex);
            if (n == k) {
                System.out.println("Problem!!!" + newIndex.toString() + " " + n + " " + k);
            }
            return new StringBuilder("0").append(recBinaryString(newIndex, n - 1, k));
        }
    }


}
