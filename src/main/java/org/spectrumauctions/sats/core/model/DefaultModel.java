/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import java.util.List;

import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public abstract class DefaultModel<W extends World, B extends Bidder<? extends Good>> {
    
    /**
     * Creates a new {@link World}
     * @param worldSeed A rng supplier for random creation of world parameters
     * @return
     */
    public abstract W createWorld(RNGSupplier worldSeed);
    
    /**
     * Creates a new {@link World}
     * @param seed The seed for the random creation of world parameters
     * @return 
     */
    public W createWorld(long seed){
        return createWorld(new JavaUtilRNGSupplier(seed));
    }
    
    /**
     * Creates a new {@link World}
     * @return
     */
    public W createWorld(){
        return createWorld(new JavaUtilRNGSupplier());
    }
    
    
    /**
     * Creates a new set of {@link Bidder} instances
     * @param world the {@link World} for which the bidders are created
     * @param populationRNG a rng supplier for the creation of random bidder parameters
     * @return
     */
    public abstract List<B> createPopulation(W world, RNGSupplier populationRNG);
    
    /**
     * Creates a new set of {@link Bidder} instances
     * @return
     */
    public List<B> createNewPopulation(){
        return createNewPopulation(new JavaUtilRNGSupplier()); 
    }
    
    /**
     * Creates a new set of {@link Bidder} instances
     * @param seed
     * @return
     */
    public List<B> createNewPopulation(long seed){
        return createNewPopulation(new JavaUtilRNGSupplier(seed)); 
    }
    
    /**
     * Creates a new set of {@link Bidder} instances for a newly generated {@link World} instance
     * @param rngSupplier A rng supplier for random creation of both world parameters and bidder paramters
     * @return
     */
    public List<B> createNewPopulation(RNGSupplier rngSupplier){
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        JavaUtilRNGSupplier worldSupplier = new JavaUtilRNGSupplier(rng.nextLong());
        JavaUtilRNGSupplier populationSupplier = new JavaUtilRNGSupplier(rng.nextLong());
        return createNewPopulation(worldSupplier, populationSupplier);
    }
    
    /**
     * Creates a new set of {@link Bidder} instances for a newly generated {@link World} instance
     * @param worldSeed A seed for random creation of world parameters
     * @param populationSeed A seed for randmon creation of bidder parameters
     * @return
     */
    public List<B> createNewPopulation(long worldSeed, long populationSeed){
        return createNewPopulation(new JavaUtilRNGSupplier(worldSeed), new JavaUtilRNGSupplier(populationSeed));
    }
    
    /**
     * Creates a new set of {@link Bidder} instances for a newly generated {@link World} instance
     * @param worldRNG A rng supplier for random creation of world parameters
     * @param populationRNG A rng supplier for randmon creation of bidder parameters
     * @return
     */
    public List<B> createNewPopulation(RNGSupplier worldRNG, RNGSupplier populationRNG){
        W world = createWorld(worldRNG);
        return createPopulation(world, populationRNG);
    }
    
    /**
     * Creates a new set of {@link Bidder} instances
     * @param world The world for which the bidders are created
     * @param populationSeed A seed for randmon creation of bidder parameters
     * @return
     */
    public List<B> createPopulation(W world, long populationSeed){
        return createPopulation(world, new JavaUtilRNGSupplier(populationSeed));
    }
    
    /**
     * Creates a new set of {@link Bidder} instances
     * @param world The world for which the bidders are created
     * @return
     */
    public List<B> createPopulation(W world){
        return createPopulation(world, new JavaUtilRNGSupplier());
    }
    
   

}
