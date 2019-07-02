/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.xor;

import com.google.common.math.BigIntegerMath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.BundleEntry;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.LicenseBundle;
import org.spectrumauctions.sats.core.model.License;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public abstract class SizeOrderedXOR implements BiddingLanguage {

    private static final Logger logger = LogManager.getLogger(SizeOrderedXOR.class);

    final List<? extends License> goods;
    private SATSBidder bidder;

    protected SizeOrderedXOR(Collection<? extends License> goods, SATSBidder bidder) {
        this.goods = new ArrayList<>(goods);
        this.bidder = bidder;
    }

    @Override
    public SATSBidder getBidder() {
        return bidder;
    }

    /**
     * @param index of the queried bundle
     */
    public Bundle getBundle(BigInteger index) {
        String bundleRepresentation = packageRepresentation(index, goods.size()).toString();
        return getBundle(bundleRepresentation);
    }

    private Bundle getBundle(String bundleRepresentation) {
        HashSet<BundleEntry> result = new HashSet<>();
        for (int i = 0; i < bundleRepresentation.length(); i++) {
            if (bundleRepresentation.charAt(i) == '1') {
                result.add(new BundleEntry(goods.get(i), 1));
            }
        }
        return new Bundle(result);
    }

    /**
     * @param subIndex an index of this bundle in a list of all bundles with same size (hence NOT the index
     *                 in the iterator), starting at zero.
     * @param size the size of the bundle
     * @return a specific bundle of given size
     */
    public Bundle getBundle(BigInteger subIndex, int size) {
        // TODO check if subIndex is valid;
        String binaryString = recBinaryString(subIndex, goods.size(), size).toString();
        return getBundle(binaryString);
    }

    /**
     * @return the StringBuilder representation of the bundle, i.e., 1/0 for all licenses
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
                logger.warn("Problem!!!" + newIndex.toString() + " " + n + " " + k);
            }
            return new StringBuilder("0").append(recBinaryString(newIndex, n - 1, k));
        }
    }


}
