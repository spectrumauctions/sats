/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.random;

import java.util.Random;

/**
 * Supplies Java Util based RNG's, and uses an own Java.Util-Based RNG for Seed
 * generation The default Seeds are not used in this Supplier
 *
 * @author Michael Weiss
 *
 */
public class JavaUtilRNGSupplier implements RNGSupplier {

    /**
     *
     */
    private static final long serialVersionUID = -8797044645698919966L;

    private final Random seedGenerator;
    private final long initSeed;

    /**
     * Takes the current timestamp as initialSeed
     */
    public JavaUtilRNGSupplier() {
        this(System.currentTimeMillis());
    }

    /**
     * Use this do define a specific initial seed, e.g. to re-run a previous
     * setting in the same build
     *
     * @param initSeed
     *            The initial Seed of the SeedGenerator
     */
    public JavaUtilRNGSupplier(long initSeed) {
        this.seedGenerator = new Random(initSeed);
        this.initSeed = initSeed;
    }

    @Override
    public UniformDistributionRNG getUniformDistributionRNG(long seed) {
        return new UniformJavaUtilRandomWrapper(seed);
    }

    @Override
    public UniformDistributionRNG getUniformDistributionRNG() {
        return new UniformJavaUtilRandomWrapper(seedGenerator.nextLong());
    }

    /**
     *
     * @return Initial Seed of this JavaUtilRNGSupplier, can be used for
     *         JavaUtilRNGSupplier(initSeed)
     */
    public long getInitSeed() {
        return initSeed;
    }

    @Override
    public GaussianDistributionRNG getGaussianDistributionRNG() {
        return new GaussianJavaUtilRandomWrapper(seedGenerator.nextLong());
    }

    @Override
    public GaussianDistributionRNG getGaussianDistributionRNG(long seed) {
        return new GaussianJavaUtilRandomWrapper(seed);
    }

}
