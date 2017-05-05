/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.random;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

public class UniformJavaUtilRandomWrapper implements UniformDistributionRNG {

    /**
     *
     */
    private static final long serialVersionUID = 4285241684660761136L;

    private Random rng;

    public UniformJavaUtilRandomWrapper() {
        this(new Date().getTime());
    }

    public UniformJavaUtilRandomWrapper(long seed) {
        this.rng = new Random(seed);
    }

    @Override
    public int nextInt() {
        return rng.nextInt();
    }

    @Override
    public int nextInt(int lowerLimit, int upperLimit) {
        if (upperLimit == Integer.MAX_VALUE)
            upperLimit--;
        return rng.nextInt((upperLimit - lowerLimit) + 1) + lowerLimit;
    }

    @Override
    public int nextInt(IntegerInterval interval) {
        return nextInt(interval.getMinValue(), interval.getMaxValue());
    }

    @Override
    public double nextDouble() {
        return rng.nextDouble();
    }

    @Override
    public double nextDouble(double lowerLimit, double upperLimit) {
        return rng.nextDouble() * (upperLimit - lowerLimit) + lowerLimit;
    }

    @Override
    public long nextLong() {
        return rng.nextLong();
    }

    @Override
    public double nextDouble(DoubleInterval interval) {
        return nextDouble(interval.getMinValue(), interval.getMaxValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see UniformDistributionRNG#nextBigDecimal()
     */
    @Override
    public BigDecimal nextBigDecimal() {
        return new BigDecimal(nextDouble());
    }

    /*
     * (non-Javadoc)
     * 
     * @see UniformDistributionRNG#nextBigDecimal(double, double)
     */
    @Override
    public BigDecimal nextBigDecimal(double lowerLimit, double upperLimit) {
        return new BigDecimal(nextDouble(lowerLimit, upperLimit));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * UniformDistributionRNG#nextBigDecimal(org.spectrumauctions.sats.core.util
     * .random.DoubleInterval)
     */
    @Override
    public BigDecimal nextBigDecimal(DoubleInterval interval) {
        return new BigDecimal(nextDouble(interval));
    }

    /*
     * (non-Javadoc)
     * 
     * @see UniformDistributionRNG#nextInt(int)
     */
    @Override
    public int nextInt(int upperLimit) {
        return rng.nextInt(upperLimit);
    }

}
