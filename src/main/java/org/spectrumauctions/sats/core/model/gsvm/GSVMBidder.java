package org.spectrumauctions.sats.core.model.gsvm;

import com.google.common.base.Preconditions;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Fabio Isler
 */
public final class GSVMBidder extends Bidder<GSVMLicense> {

    private static final long serialVersionUID = -7275733600491984673L;
    private final int bidderPosition;
    private final HashMap<Long, BigDecimal> values;
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
        if (!bundle.isEmpty()) factor = 0.2 * (bundle.size() - 1);
        return BigDecimal.valueOf(value + value * factor);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GSVMBidder that = (GSVMBidder) o;
        return getBidderPosition() == that.getBidderPosition() &&
                Objects.equals(values, that.values) &&
                Objects.equals(getWorld(), that.getWorld());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getBidderPosition(), values, getWorld());
    }

    public Map<Long, BigDecimal> getBaseValues() {
        return Collections.unmodifiableMap(values);
    }

}
