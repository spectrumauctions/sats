package ch.uzh.ifi.ce.mweiss.sats.core.model.cats;

import ch.uzh.ifi.ce.mweiss.sats.core.model.World;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.BiddingLanguage;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bundle;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.JavaUtilRNGSupplier;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public final class CATSBidder extends Bidder<CATSLicense> {

    private final Map<Long, BigDecimal> privateValues;
    private transient CATSWorld world;
    private transient ImmutableMap<Long, BigDecimal> privateValueMap;


    CATSBidder(CATSBidderSetup setup, CATSWorld world, long currentId, long population, RNGSupplier rngSupplier) {
        super(setup, population, currentId, world.getId());
        this.world = world;
        this.privateValues = setup.drawPrivateValues(rngSupplier, this);
        store();
    }

    @Override
    public BigDecimal calculateValue(Bundle<CATSLicense> bundle) {
        double value = 0;
        for (CATSLicense license : bundle) {
            if (this.privateValues.containsKey(license.getId())) {
                value += license.getCommonValue();
                value += this.privateValues.get(license.getId()).doubleValue();
                /*
                 * This quadratic pricing option doesn't seem to be implemented in CATS, just mentioned in the paper.
                 * This is how we assume it would have been implemented, according to the author's remarks.
                 */
                if (getWorld().getUseQuadraticPricingOption()) {
                    value += Math.pow(license.getCommonValue(), 2);
                }
            }
        }
        if (!getWorld().getUseQuadraticPricingOption()) {
            value += Math.pow(bundle.size(), 1 + world.getAdditivity());
        }
        return new BigDecimal(value);
    }


    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> clazz, long seed) throws UnsupportedBiddingLanguageException {
        if (clazz.isAssignableFrom(SizeBasedUniqueRandomXOR.class)) {
            return clazz.cast(
                    new SizeBasedUniqueRandomXOR<>(world.getLicenses(), new JavaUtilRNGSupplier(seed), this));
        } else if (clazz.isAssignableFrom(IncreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new IncreasingSizeOrderedXOR<>(world.getLicenses(), this));
        } else if (clazz.isAssignableFrom(DecreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new DecreasingSizeOrderedXOR<>(world.getLicenses(), this));
        } else {
            throw new UnsupportedBiddingLanguageException();
        }
    }

    @Override
    public CATSWorld getWorld() {
        return this.world;
    }

    public ImmutableMap<Long, BigDecimal> getPrivateValues() {
        if (privateValueMap == null) {
            privateValueMap = ImmutableMap.copyOf(privateValues);
        }
        return privateValueMap;
    }

    @Override
    public void refreshReference(World world) {
        Preconditions.checkArgument(world.getId() == getWorldId());
        if (world instanceof CATSWorld) {
            this.world = (CATSWorld) world;
        } else {
            throw new IllegalArgumentException("World is not of correct type");
        }
    }

}
