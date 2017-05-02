package ch.uzh.ifi.ce.mweiss.sats.core.model.lsvm;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.BiddingLanguage;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bundle;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.World;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.JavaUtilRNGSupplier;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabio Isler
 */
public final class LSVMBidder extends Bidder<LSVMLicense> {

    private final int LSVM_A;
    private final int LSVM_B;
    private final Set<LSVMLicense> proximity;
    private final Map<Long, BigDecimal> values;
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
            double factor = 1 + (LSVM_A / (100 * (1 + Math.exp(LSVM_B - subset.size()))));
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

    /* (non-Javadoc)
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
}
