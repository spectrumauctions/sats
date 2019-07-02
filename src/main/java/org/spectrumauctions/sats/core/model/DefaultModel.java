/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public abstract class DefaultModel<W extends World, B extends SATSBidder<? extends License>> {

    /**
     * Creates a new {@link World}
     * @param worldSeed A rng supplier for random creation of world parameters
     * @return a new world
     */
    public abstract W createWorld(RNGSupplier worldSeed);

    /**
     * Creates a new {@link World}
     * @param seed The seed for the random creation of world parameters
     * @return a new world
     */
    public W createWorld(long seed) {
        return createWorld(new JavaUtilRNGSupplier(seed));
    }

    /**
     * Creates a new {@link World}
     * @return a new world
     */
    public W createWorld() {
        return createWorld(new JavaUtilRNGSupplier());
    }


    /**
     * Creates a new set of {@link SATSBidder} instances
     * @param world the {@link World} for which the bidders are created
     * @param populationRNG a rng supplier for the creation of random bidder parameters
     * @return a new set of bidders
     */
    public abstract List<B> createPopulation(W world, RNGSupplier populationRNG);

    /**
     * Creates a new set of {@link SATSBidder} instances
     * @return a new set of bidders
     */
    public List<B> createNewPopulation() {
        return createNewPopulation(new JavaUtilRNGSupplier());
    }

    /**
     * Creates a new set of {@link SATSBidder} instances
     * @param seed the seed for the RNG
     * @return a new set of bidders
     */
    public List<B> createNewPopulation(long seed) {
        return createNewPopulation(new JavaUtilRNGSupplier(seed));
    }

    /**
     * Creates a new set of {@link SATSBidder} instances for a newly generated {@link World} instance
     * @param rngSupplier A rng supplier for random creation of both world parameters and bidder paramters
     * @return a new set of bidders
     */
    public List<B> createNewPopulation(RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        JavaUtilRNGSupplier worldSupplier = new JavaUtilRNGSupplier(rng.nextLong());
        JavaUtilRNGSupplier populationSupplier = new JavaUtilRNGSupplier(rng.nextLong());
        return createNewPopulation(worldSupplier, populationSupplier);
    }

    /**
     * Creates a new set of {@link SATSBidder} instances for a newly generated {@link World} instance
     * @param worldSeed A seed for random creation of world parameters
     * @param populationSeed A seed for randmon creation of bidder parameters
     * @return a new set of bidders
     */
    public List<B> createNewPopulation(long worldSeed, long populationSeed) {
        return createNewPopulation(new JavaUtilRNGSupplier(worldSeed), new JavaUtilRNGSupplier(populationSeed));
    }

    /**
     * Creates a new set of {@link SATSBidder} instances for a newly generated {@link World} instance
     * @param worldRNG A rng supplier for random creation of world parameters
     * @param populationRNG A rng supplier for randmon creation of bidder parameters
     * @return a new set of bidders
     */
    public List<B> createNewPopulation(RNGSupplier worldRNG, RNGSupplier populationRNG) {
        W world = createWorld(worldRNG);
        return createPopulation(world, populationRNG);
    }

    /**
     * Creates a new set of {@link SATSBidder} instances
     * @param world The world for which the bidders are created
     * @param populationSeed A seed for random creation of bidder parameters
     * @return a new set of bidders
     */
    public List<B> createPopulation(W world, long populationSeed) {
        return createPopulation(world, new JavaUtilRNGSupplier(populationSeed));
    }

    /**
     * Creates a new set of {@link SATSBidder} instances
     * @param world The world for which the bidders are created
     * @return a new set of bidders
     */
    public List<B> createPopulation(W world) {
        return createPopulation(world, new JavaUtilRNGSupplier());
    }


}
