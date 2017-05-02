package ch.uzh.ifi.ce.mweiss.specval.model.gsvm;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.BiddingLanguage;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.DecreasingSizeOrderedXOR;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.IncreasingSizeOrderedXOR;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.SizeBasedUniqueRandomXOR;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.World;
import ch.uzh.ifi.ce.mweiss.specval.util.random.JavaUtilRNGSupplier;
import ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier;

/**
 * @author Fabio Isler
 */
public final class GSVMBidder extends Bidder<GSVMLicense> {

    private final int bidderPosition;
    private final Map<Long, BigDecimal> values;
    private transient GSVMWorld world;

    GSVMBidder(GSVMBidderSetup setup, GSVMWorld world, int bidderPosition, long currentId, long population, RNGSupplier rngSupplier) {
        super(setup, population, currentId, world.getId());
        this.world = world;
        this.bidderPosition = bidderPosition % world.getSize();
        this.values = setup.drawValues(rngSupplier, this);

        store();
    }

    @Override
    public BigDecimal calculateValue(Bundle<GSVMLicense> bundle) {
        double value = 0;
        for (GSVMLicense license : bundle) {
            if (this.values.containsKey(license.getId())) {
                value += this.values.get(license.getId()).doubleValue();
            }
        }
        double factor = 0;
        if (bundle.size() > 0) factor = 0.2 * (bundle.size() - 1);
        return new BigDecimal(value + value * factor);
    }

    public int getBidderPosition() {
        return bidderPosition;
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
    public GSVMWorld getWorld() {
        return this.world;
    }

    @Override
    public void refreshReference(World world) {
        Preconditions.checkArgument(world.getId() == getWorldId());
        if (world instanceof GSVMWorld) {
            this.world = (GSVMWorld) world;
        } else {
            throw new IllegalArgumentException("World is not of correct type");
        }
    }
    
    public Map<Long, BigDecimal> getBaseValues(){
    	return Collections.unmodifiableMap(values); 
    }

}
