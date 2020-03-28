package org.spectrumauctions.sats.core.model.cats;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.NotImplementedException;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.price.Prices;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.xor.CatsXOR;
import org.spectrumauctions.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * @author Fabio Isler
 */
@EqualsAndHashCode(callSuper = true)
public final class CATSBidder extends SATSBidder {

    private static final long serialVersionUID = -6762037404466323951L;
    private final HashMap<Long, BigDecimal> privateValues;
    @EqualsAndHashCode.Exclude
    private transient CATSWorld world;
    @EqualsAndHashCode.Exclude
    private transient ImmutableMap<Long, BigDecimal> privateValueMap;


    CATSBidder(CATSBidderSetup setup, CATSWorld world, long currentId, long population, RNGSupplier rngSupplier) {
        super(setup, population, currentId, world.getId());
        this.world = world;
        this.privateValues = setup.drawPrivateValues(rngSupplier, this);
        store();
    }

    @Override
    public BigDecimal calculateValue(Bundle bundle) {
        double value = 0;
        for (Good good : bundle.getSingleQuantityGoods()) {
            CATSLicense license = (CATSLicense) good;
            if (this.privateValues.containsKey(license.getLongId())) {
                value += license.getCommonValue();
                value += this.privateValues.get(license.getLongId()).doubleValue();
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
            value += Math.pow(bundle.getSingleQuantityGoods().size(), 1 + world.getAdditivity());
        }
        return BigDecimal.valueOf(value);
    }


    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> clazz, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        if (clazz.isAssignableFrom(CatsXOR.class)) {
            return clazz.cast(new CatsXOR(world.getLicenses(), rngSupplier, this));
        } else if (clazz.isAssignableFrom(SizeBasedUniqueRandomXOR.class)) {
            return clazz.cast(
                    new SizeBasedUniqueRandomXOR(world.getLicenses(), rngSupplier, this));
        } else if (clazz.isAssignableFrom(IncreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new IncreasingSizeOrderedXOR(world.getLicenses(), this));
        } else if (clazz.isAssignableFrom(DecreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new DecreasingSizeOrderedXOR(world.getLicenses(), this));
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

    @Override
    public SATSBidder drawSimilarBidder(RNGSupplier rngSupplier) {
        return new CATSBidder((CATSBidderSetup) getSetup(), getWorld(), getLongId(), getPopulation(), rngSupplier);
    }

    @Override
    public List<Bundle> getBestBundles(Prices prices, int maxNumberOfBundles, boolean allowNegative) {
        throw new NotImplementedException("Demand Query to be implemented");
    }
}
