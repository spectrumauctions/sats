/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.UniformDistributionRNG;
import com.google.common.math.BigIntegerMath;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.MissingInformationException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bundle;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.GaussianDistributionRNG;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;

public class SizeBasedUniqueRandomXOR<T extends Good> implements XORLanguage<T> {
    private int meanBundleSize = -1;
    private double standardDeviation = -1;
    private Collection<T> goods;
    private long seed;
    private final RNGSupplier rngSupplier;
    private int iterations = -1;
    private Bidder<T> bidder;

    public SizeBasedUniqueRandomXOR(Collection<T> goods, RNGSupplier rngSupplier, Bidder<T> bidder) {
        this.goods = goods;
        this.seed = rngSupplier.getUniformDistributionRNG().nextLong();
        this.rngSupplier = rngSupplier;
        this.bidder = bidder;
    }

    protected BigDecimal getValue(Bundle<T> goods){
        return bidder.calculateValue(goods);
    }

    /* (non-Javadoc)
     * @see BiddingLanguage#getBidder()
     */
    @Override
    public Bidder<T> getBidder() {
        return bidder;
    }
    
    
    public void setDefaultDistribution(){
        this.meanBundleSize = goods.size()/2;
        this.standardDeviation = meanBundleSize/2;
        int exponent = goods.size() < 13 ? goods.size() : 13;
        this.iterations = (int)Math.pow(2, exponent)-1;
    }
    
    /**
     * Set the basic properties of this iterator.
     * Note that the parameters are not checked for its validity and meaningfulness.
     * 
     * @param meanBundleSize
     *            : The mean bundle size of the randomly generated packages. Should by >0 and <= the number of goods.
     * @param standardDeviation
     *            : The bundle size standard deviation
     * @param iterations
     *            : The number of iterations before iterator.hasNext() returns false. Note that setting this parameter
     *            too high will result in a slow iterator and possibly cause a StackOverflowException while iterating.
     */
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
    public Iterator<XORValue<T>> iterator() {
        if (meanBundleSize < 0 || standardDeviation < 0 || iterations < 0) {
            setDefaultDistribution();
        }
        return new ValueIterator(rngSupplier.getUniformDistributionRNG(seed),
                rngSupplier.getGaussianDistributionRNG(seed + 1), meanBundleSize, standardDeviation, iterations);
    }

    private class BigIntegerComparator implements Comparator<BigInteger> {
        @Override
        public int compare(BigInteger o1, BigInteger o2) {
            return o1.compareTo(o2);
        }
    }

    /**
     * Rurns a bigInteger between 0 and maxValue (both inclusive)
     * 
     * @param maxValue
     * @return
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

    private class ValueIterator implements Iterator<XORValue<T>> {
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
            remainingBundles = new ArrayList<BigInteger>(numberOfGoods - 1);
            while (remainingBundles.size() < numberOfGoods - 1)
                remainingBundles.add(BigInteger.ZERO);

            // TODO To fasten things up, use the symmetry properties of binomial coefficient (symmetric pyramid of
            // values), saving half of calculations.
            for (int bundleSize = 1; bundleSize <= numberOfGoods; bundleSize++) {
                generatedBundleNumbers.put(bundleSize, new TreeSet<BigInteger>(new BigIntegerComparator()));
                BigInteger numberOfBundles = BigIntegerMath.binomial(numberOfGoods, bundleSize);
                remainingBundles.add(bundleSize - 1, numberOfBundles);
            }

        }

        @Override
        public boolean hasNext() {
            return remainingIterations > 0;
        }

        @Override
        public XORValue<T> next() {
            // Check if bundles available and update remaining number of bundles
            if (!hasNext())
                throw new NoSuchElementException();
            remainingIterations--;

            return recNext();
        }

        private XORValue<T> recNext() {

            // Determine Bundle Size. TODO: This has to be done nicer, avoiding picking a nonexisting/nonavailable size
            int bundleSize = (int) Math.round(gaussRng.nextGaussian(meanBundleSize, standardDeviation));
            if (bundleSize < 1 || bundleSize > goods.size())
                return recNext();

            BigInteger remainingBundlesOfThisSize = remainingBundles.get(bundleSize - 1);

            // Check if Bundle out of feasible range (bigger than available items or size <= 0), If so, recurse.
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
            SizeOrderedXOR<T> sizeOrderedXOR = new IncreasingSizeOrderedXOR<>(new Bundle<T>(goods), getBidder());
            Bundle<T> bundle = sizeOrderedXOR.getBundle(bundleId, bundleSize);
            return new XORValue<T>(bundle, getValue(bundle));
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
