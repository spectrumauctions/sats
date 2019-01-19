package org.spectrumauctions.sats.core.model.lsvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Fabio Isler
 */
public final class LSVMBidder extends Bidder<LSVMLicense> {

    private static final long serialVersionUID = -1774118565772856391L;
    private final int LSVM_A;
    private final int LSVM_B;
    private final Set<LSVMLicense> proximity;
    private final HashMap<Long, BigDecimal> values;
    private transient LSVMWorld world;

    LSVMBidder(LSVMBidderSetup setup, LSVMWorld world, long currentId, long population, RNGSupplier rngSupplier) {
        super(setup, population, currentId, world.getId());
        this.world = world;
        LSVMLicense favorite = setup.drawFavorite(rngSupplier, world);
        if (setup.getProximitySize() == -1) { // It's a national bidder
            this.proximity = world.getGrid().getLicenses();
        } else { // It's a regional bidder
            this.proximity = world.getGrid().getProximity(favorite, setup.getProximitySize());
        }
        this.values = setup.drawValues(rngSupplier, this);
        this.LSVM_A = setup.getLsvmA();
        this.LSVM_B = setup.getLsvmB();
        store();
    }

    public ImmutableSet<LSVMLicense> getProximity() {
        return ImmutableSet.copyOf(proximity);
    }

    @Override
    public BigDecimal calculateValue(Bundle<LSVMLicense> bundle) {
        double value = 0;
        Set<Set<LSVMLicense>> subpackages = world.getGrid().getMaximallyConnectedSubpackages(bundle);
        for (Set<LSVMLicense> subset : subpackages) {
            double factor = calculateFactor(subset.size());
            value += factor * sumOfItemValues(subset);
        }
        return new BigDecimal(value);
    }

    private double sumOfItemValues(Set<LSVMLicense> subset) {
        double value = 0;
        for (LSVMLicense license : subset) {
            if (this.values.containsKey(license.getId())) {
                value += this.values.get(license.getId()).doubleValue();
            }
        }
        return value;
    }

    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> clazz, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        if (clazz.isAssignableFrom(SizeBasedUniqueRandomXOR.class)) {
            return clazz.cast(
                    new SizeBasedUniqueRandomXOR<>(world.getLicenses(), rngSupplier, this));
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

    /**
     * @see Bidder#getWorld()
     */
    @Override
    public LSVMWorld getWorld() {
        return world;
    }

    @Override
    public void refreshReference(World world) {
        Preconditions.checkArgument(world.getId() == getWorldId());
        if (world instanceof LSVMWorld) {
            this.world = (LSVMWorld) world;
        } else {
            throw new IllegalArgumentException("World is not of correct type");
        }
    }

    @Override
    public Bidder<LSVMLicense> drawSimilarBidder(RNGSupplier rngSupplier) {
        return new LSVMBidder((LSVMBidderSetup) getSetup(), getWorld(), getId(), getPopulation(), rngSupplier);
    }

    public Map<Long, BigDecimal> getBaseValues() {
        return Collections.unmodifiableMap(values);
    }
    
    public double calculateFactor(int size){
    	return 1 + (LSVM_A / (100 * (1 + Math.exp(LSVM_B - size))));
    }
}
