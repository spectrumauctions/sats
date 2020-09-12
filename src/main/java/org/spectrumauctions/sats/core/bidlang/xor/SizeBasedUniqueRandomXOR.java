/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.xor;

import com.google.common.math.BigIntegerMath;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.bidder.valuefunction.BundleValue;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.MissingInformationException;
import org.spectrumauctions.sats.core.model.License;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.util.random.GaussianDistributionRNG;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class SizeBasedUniqueRandomXOR implements BiddingLanguage {
    private int meanBundleSize = -1;
    private double standardDeviation = -1;
    private Collection<? extends License> goods;
    private long seed;
    private final RNGSupplier rngSupplier;
    private int iterations = -1;
    private SATSBidder bidder;

    public SizeBasedUniqueRandomXOR(Collection<? extends License> goods, RNGSupplier rngSupplier, SATSBidder bidder) {
        this.goods = goods;
        this.seed = rngSupplier.getUniformDistributionRNG().nextLong();
        this.rngSupplier = rngSupplier;
        this.bidder = bidder;
    }

    protected BigDecimal getValue(Bundle goods) {
        return bidder.calculateValue(goods);
    }

    @Override
    public SATSBidder getBidder() {
        return bidder;
    }

    public void setDefaultDistribution() {
        this.meanBundleSize = goods.size() / 2;
        this.standardDeviation = meanBundleSize / 2.;
    }

    public void setMaxIterations() {
        int exponent = goods.size() < 13 ? goods.size() : 13;
        this.iterations = (int) Math.pow(2, exponent) - 1;
    }

   /**
    * Set the number of iterations of this iterator.
    *
    * @param iterations
    *            : The number of iterations before iterator.hasNext() returns false. Note that setting this parameter
    *            too high will result in a slow iterator and possibly cause a StackOverflowException while iterating.
    */

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * Set the basic distribution of this iterator.
     * Note that the parameters are not checked for its validity and meaningfulness.
     *
     * @param meanBundleSize
     *            : The mean bundle size of the randomly generated packages. Should by greater than 0 and less than or equal to the number of goods.
     * @param standardDeviation
     *            : The bundle size standard deviation
     */
    public void setDistribution(int meanBundleSize, double standardDeviation) {
        this.meanBundleSize = meanBundleSize;
        this.standardDeviation = standardDeviation;
    }

    @Deprecated
    public void setDistribution(int meanBundleSize, double standardDeviation, int iterations) {
        this.meanBundleSize = meanBundleSize;
        this.standardDeviation = standardDeviation;
        this.iterations = iterations;
    }

    /**
     * {@inheritDoc} Throws a {@link MissingInformationException} if the method
     * {@link #setDistribution(int, double, int)} was not called before this operation.
     */
    @Override
    public Iterator<BundleValue> iterator() {
        if (meanBundleSize < 0 || standardDeviation < 0) {
            setDefaultDistribution();
        }
        if (iterations < 0) {
            setMaxIterations();
        }
        return new ValueIterator(rngSupplier.getUniformDistributionRNG(seed),
                rngSupplier.getGaussianDistributionRNG(seed + 1), meanBundleSize, standardDeviation, iterations);
    }

    private class BigIntegerComparator implements Comparator<BigInteger>, Serializable{
        @Override
        public int compare(BigInteger o1, BigInteger o2) {
            return o1.compareTo(o2);
        }
    }

    /**
     * @return a bigInteger between 0 and maxValue (both inclusive)
     */
    private BigInteger randomBigInteger(BigInteger maxValue, long seed) {
        Random rnd = new Random(seed);
        BigInteger random;
        do {
            random = new BigInteger(maxValue.bitLength(), rnd);
            // compare random number lessthan given number
        } while (random.compareTo(maxValue) > 0);
        return random;
    }

    private class ValueIterator implements Iterator<BundleValue> {
        final Map<Integer, SortedSet<BigInteger>> generatedBundleNumbers = new HashMap<>();
        final List<BigInteger> remainingBundles;
        final int numberOfGoods;
        private final UniformDistributionRNG uniRng;
        private final GaussianDistributionRNG gaussRng;
        private final int meanBundleSize;
        private final double stdDeviation;
        private int remainingIterations;

        public ValueIterator(UniformDistributionRNG uniRng, GaussianDistributionRNG gaussRng, int meanBundleSize,
                             double stdDeviation, int iterations) {
            this.uniRng = uniRng;
            this.gaussRng = gaussRng;

            this.meanBundleSize = meanBundleSize;
            this.stdDeviation = stdDeviation;
            this.remainingIterations = iterations;

            numberOfGoods = SizeBasedUniqueRandomXOR.this.goods.size();
            remainingBundles = new ArrayList<>(numberOfGoods - 1);
            while (remainingBundles.size() < numberOfGoods - 1)
                remainingBundles.add(BigInteger.ZERO);

            // TODO To fasten things up, use the symmetry properties of binomial coefficient (symmetric pyramid of
            // values), saving half of calculations.
            for (int bundleSize = 1; bundleSize <= numberOfGoods; bundleSize++) {
                generatedBundleNumbers.put(bundleSize, new TreeSet<>(new BigIntegerComparator()));
                BigInteger numberOfBundles = BigIntegerMath.binomial(numberOfGoods, bundleSize);
                remainingBundles.add(bundleSize - 1, numberOfBundles);
            }

        }

        @Override
        public boolean hasNext() {
            return remainingIterations > 0;
        }

        @Override
        public BundleValue next() {
            // Check if bundles available and update remaining number of bundles
            if (!hasNext())
                throw new NoSuchElementException();
            remainingIterations--;

            return recNext();
        }

        private BundleValue recNext() {

            // Determine LicenseBundle Size. TODO: This has to be done nicer, avoiding picking a nonexisting/nonavailable size
            int bundleSize = (int) Math.round(gaussRng.nextGaussian(meanBundleSize, standardDeviation));
            if (bundleSize < 1 || bundleSize > goods.size())
                return recNext();

            BigInteger remainingBundlesOfThisSize = remainingBundles.get(bundleSize - 1);

            // Check if LicenseBundle out of feasible range (bigger than available items or size <= 0), If so, recurse.
            if (remainingBundlesOfThisSize == null) {
                return recNext();
            }

            // Check if bundles of this size are available
            if (remainingBundlesOfThisSize.compareTo(BigInteger.ZERO) <= 0) {
                return recNext();
            }

            // determine bundle id
            BigInteger bundleId = randomBigInteger(remainingBundlesOfThisSize, uniRng.nextLong());
            for (BigInteger previouslyGeneratedIndex : generatedBundleNumbers.get(bundleSize)) {
                if (bundleId.compareTo(previouslyGeneratedIndex) >= 0) {
                    bundleId = bundleId.add(BigInteger.ONE);
                } else {
                    // We can break as the list is sorted
                    break;
                }
            }
            // Update index info
            generatedBundleNumbers.get(bundleSize).add(bundleId);
            BigInteger newlyAvailable = remainingBundles.remove(bundleSize - 1).subtract(BigInteger.ONE);
            remainingBundles.add(bundleSize - 1, newlyAvailable);

            // Return result
            SizeOrderedXOR sizeOrderedXOR = new IncreasingSizeOrderedXOR(goods, getBidder());
            Bundle bundle = sizeOrderedXOR.getBundle(bundleId, bundleSize);
            return new BundleValue(bidder.calculateValue(bundle), bundle);
        }
    }


}
