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
public abstract class DefaultModel<W extends World, B extends SATSBidder> {

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
     * Default version if you do not have to keep track of the seeds of your auction instance.
     * Note your experiments will not be repeatable with this version.
     * Creates a new set of {@link SATSBidder} instances
     * @return a new set of bidders
     */
    public List<B> createNewWorldAndPopulation() {
    	return createNewWorldAndPopulation(new JavaUtilRNGSupplier());
    }

    /**
     * Default version if you want to keep track of the seeds of your auction instances.
     * In practice you want you to use this version to achieve repeatable experiments. 
     * Creates a new set of {@link SATSBidder} instances
     * @param seed the seed for the RNG
     * @return a new set of bidders
     */
    public List<B> createNewWorldAndPopulation(long seed) {
        return createNewWorldAndPopulation(seed, seed+1);
    }

    /**
     * 
     * Creates a new set of {@link SATSBidder} instances for a newly generated {@link World} instance
     * @param rngSupplier A rng supplier for random creation of both world parameters and bidder paramters
     * @return a new set of bidders
     */
    @Deprecated
    public List<B> createNewWorldAndPopulation(RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        JavaUtilRNGSupplier worldSupplier = new JavaUtilRNGSupplier(rng.nextLong());
        JavaUtilRNGSupplier populationSupplier = new JavaUtilRNGSupplier(rng.nextLong());
        return createNewWorldAndPopulation(worldSupplier, populationSupplier);
    }

    /**
     * Creates a new set of {@link SATSBidder} instances for a newly generated {@link World} instance
     * If you are only interested in one population of the world you might rather use the method, 
     * where you only provide one seed. When you are interested in different populations of the same
     * world you may use the method where you provide the already created world
     * @param worldSeed A seed for random creation of world parameters
     * @param populationSeed A seed for random creation of bidder parameters
     * @return a new set of bidders
     * @see #createNewWorldAndPopulation(long)
     * @see #createNewPopulation(World, long)
     */
    @Deprecated
    public List<B> createNewWorldAndPopulation(long worldSeed, long populationSeed) {
        return createNewWorldAndPopulation(new JavaUtilRNGSupplier(worldSeed), new JavaUtilRNGSupplier(populationSeed));
    }

    /**
     * Creates a new set of {@link SATSBidder} instances for a newly generated {@link World} instance
     * @param worldRNG A rng supplier for random creation of world parameters
     * @param populationRNG A rng supplier for randmon creation of bidder parameters
     * @return a new set of bidders
     */
    public List<B> createNewWorldAndPopulation(RNGSupplier worldRNG, RNGSupplier populationRNG) {
        W world = createWorld(worldRNG);
        return createPopulation(world, populationRNG);
    }

    /**
     * Creates a new set of {@link SATSBidder} instances
     * @param world The world for which the bidders are created
     * @param populationSeed A seed for random creation of bidder parameters
     * @return a new set of bidders
     */
    public List<B> createNewPopulation(W world, long populationSeed) {
        return createPopulation(world, new JavaUtilRNGSupplier(populationSeed));
    }

    /**
     * Creates a new set of {@link SATSBidder} instances
     * @param world The world for which the bidders are created
     * @return a new set of bidders
     */
    public List<B> createNewPopulation(W world) {
        return createPopulation(world, new JavaUtilRNGSupplier());
    }


}