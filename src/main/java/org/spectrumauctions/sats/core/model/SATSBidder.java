/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.allocationlimits.validators.AllocationLimitUtils;
import org.marketdesignresearch.mechlib.core.bidder.Bidder;
import org.marketdesignresearch.mechlib.core.bidder.newstrategy.DefaultStrategyHandler;
import org.marketdesignresearch.mechlib.core.bidder.newstrategy.InteractionStrategy;
import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.util.instancehandling.InstanceHandler;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.springframework.data.annotation.Persistent;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
public abstract class SATSBidder implements Bidder, Serializable {

    private static final long serialVersionUID = 3424512863538320455L;
    @EqualsAndHashCode.Include
    private final String setupType;
    private final UUID uuid;
    private final long population;
    @EqualsAndHashCode.Include
    private final long id;
    private final long worldId;
    private transient final BidderSetup setup;
    
    protected static final double DEFAULT_DEMAND_QUERY_EPSILON = 1e-10;
    protected static final double DEFAULT_DEMAND_QUERY_TIME_LIMIT = 600;

    protected SATSBidder(BidderSetup setup, long population, long id, long worldId) {
        this.uuid = UUID.randomUUID();
        this.setup = setup;
        this.setupType = setup.getSetupName();
        this.id = id;
        this.population = population;
        this.worldId = worldId;
    }

    public long getLongId() {
        return id;
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public String toString() {
        return "SATSBidder{" +
                getDescription() +
                '}';
    }

    @Override
    public String getDescription() {
        return getShortDescription();
    }

    @Override
    public String getShortDescription() {
        return getSetupType() + ": " + getName();
    }

    /**
     * @return the name of the configuration ({@link BidderSetup}) (i.e., the 'bidder type') with which the bidder was created.
     */
    public String getSetupType() {
        return setupType;
    }


//    /**
//     * Returns the value this bidder has for a specific bundle.
//     * Attention: May throw RuntimeExceptions if the items in the bundle are not of the correct world.
//     *
//     * @param bundle the bundle for which the value is asked
//     * @return bidder specific value for this bundle
//     */
//    @Deprecated
//    public double getValue(LicenseBundle<?> bundle) {
//        if (bundle.getWorld().equals(this.getWorld())) {
//            throw new IncompatibleWorldException("Bundle not from the same world as the bidder");
//        }
//        return calculateValue(bundle).doubleValue();
//    }

    /**
     * Returns the value this bidder has for a specific bundle.
     *
     * @param bundle the bundle for which the value is asked
     * @return bidder specific value for this bundle
     */
    public abstract BigDecimal calculateValue(Bundle bundle);

    @Override
    public BigDecimal getValue(Bundle bundle, boolean ignoreAllocationLimits) {
    	Preconditions.checkArgument(ignoreAllocationLimits || AllocationLimitUtils.HELPER.validate(this.getAllocationLimit(), bundle));
        return calculateValue(bundle);
    }

    /**
     * Use this method to get a desired value function representation (bidding language)
     * for this bidder.
     * Note that this method may cause your compiler to throw a warning, as the generics of generic
     * Bidding Languages (such as XOR) is not specified in .class.
     *
     * @param type the type of the value function
     * @return the value function of this bidder
     * @throws UnsupportedBiddingLanguageException
     *             Throws this exception for all bidding languages
     *             which are not supported by the implementing bidder class. This javadoc should be extended by
     *             the implementing class, specifying which value function representation are supported.
     */
    public <T extends BiddingLanguage> T getValueFunction(Class<T> type)
            throws UnsupportedBiddingLanguageException {
        return getValueFunction(type, new Date().getTime());
    }


    /**
     * Use this method to get a desired value function representation (bidding language)
     * for this bidder.
     * Note that this method may cause your compiler to throw a warning, as the generics of generic
     * Bidding Languages (such as XOR) is not specified in .class.
     * generic
     *
     * @param type the type of the value function
     * @return the value function of this bidder
     * @throws UnsupportedBiddingLanguageException
     *             Throws this exception for all bidding languages
     *             which are not supported by the implementing bidder class. This javadoc should be extended by
     *             the implementing class, specifying which value function representation are supported.
     */
    public <T extends BiddingLanguage> T getValueFunction(Class<T> type, long seed)
            throws UnsupportedBiddingLanguageException {
        return getValueFunction(type, new JavaUtilRNGSupplier(seed));
    }

    public abstract <T extends BiddingLanguage> T getValueFunction(Class<T> type, RNGSupplier rngSupplier)
            throws UnsupportedBiddingLanguageException;

    /**
     * @return the population to which this bidder belongs.
     * The population is not meaningful, if for a specific world instance, only one set of Bidders
     * is created. However, if there are multiple sets of bidders (which should not be part of the same simulation)
     * created, they have different population ids.
     */
    public long getPopulation() {
        return population;
    }

    /**
     * @return World to which this bidder belongs.
     * The implementing Bidder class, overriding this method,
     * should return a world type corresponding to the specific model.
     */
    public abstract World getWorld();

    protected void store() {
        InstanceHandler.getDefaultHandler().writeBidder(this);
    }


    /**
     * To prevent from creating too many identical world instances, worlds are not serialized and deserialized with the bidder<br>
     * As a temporary solution, the world instance is re-added after deserialization by calling this method.<br><br>
     *
     * This method will be removed in a later version and be done automatically during deserialization.
     */
    public abstract void refreshReference(World world);

    public long getWorldId() {
        return worldId;
    }

    public abstract SATSBidder drawSimilarBidder(RNGSupplier rngSupplier);

    protected BidderSetup getSetup() {
        return setup;
    }
    
    // region strategy
    // TODO handle persistence
    private Map<Class<? extends InteractionStrategy>,InteractionStrategy> strategies = new HashMap<>();
    
    @Override
	public void setStrategy(InteractionStrategy strategy) {
    	strategy.setBidder(this);
		strategy.getTypes().forEach(t -> this.strategies.put(t, strategy));
	}
    
    @SuppressWarnings("unchecked")
	@Override
	public <T extends InteractionStrategy> T getStrategy(Class<T> type) {
		if(!this.strategies.containsKey(type)) this.setStrategy(DefaultStrategyHandler.defaultStrategy(type));
		return  (T) this.strategies.get(type);
	}
	// endregion

    // region instrumentation
    @Getter @Setter
    private MipInstrumentation mipInstrumentation = MipInstrumentation.NO_OP;
    // endregion

}
