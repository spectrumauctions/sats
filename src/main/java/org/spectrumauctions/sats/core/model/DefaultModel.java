/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public abstract class DefaultModel<W extends World, B extends Bidder<? extends Good>> {

    /**
     * Creates a new {@link World}
     * @return a new world
     */
    public W createWorld() {
        return createWorld(new JavaUtilRNGSupplier());
    }

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
     * @param worldSeed A rng supplier for random creation of world parameters
     * @return a new world
     */
    public abstract W createWorld(RNGSupplier worldSeed);

    /**
     * Creates a new set of {@link Bidder} instances randomly
     * @return a new set of bidders
     */
    public List<B> createPopulation() {
        return createPopulation(createWorld());
    }

    /**
     * Creates a new set of {@link Bidder} instances
     * @param world The world for which the bidders are created
     * @return a new set of bidders
     */
    public List<B> createPopulation(W world) {
        return createPopulation(world, new JavaUtilRNGSupplier());
    }

    /**
     * Creates a new set of {@link Bidder} instances
     * @param world The world for which the bidders are created
     * @param populationSeed A seed for random creation of bidder parameters
     * @return a new set of bidders
     */
    public List<B> createPopulation(W world, long populationSeed) {
        return createPopulation(world, new JavaUtilRNGSupplier(populationSeed));
    }

    /**
     * Creates a new set of {@link Bidder} instances
     * @param worldSeed The seed for random creation of the world for which the bidders are created
     * @param populationSeed A seed for random creation of bidder parameters
     * @return a new set of bidders
     */
    public List<B> createPopulation(long worldSeed, long populationSeed) {
        return createPopulation(createWorld(worldSeed), populationSeed);
    }

    /**
     * Creates a new set of {@link Bidder} instances
     * @param superSeed The seed for random creation of the world and the bidders
     * @return a new set of bidders
     */
    public List<B> createPopulation(long superSeed) {
        return createPopulation(createWorld(superSeed), superSeed);
    }

    /**
     * Creates a new set of {@link Bidder} instances
     * @param world the {@link World} for which the bidders are created
     * @param populationRNG a rng supplier for the creation of random bidder parameters
     * @return a new set of bidders
     */
    public abstract List<B> createPopulation(W world, RNGSupplier populationRNG);

}
