package org.spectrumauctions.sats.core.model.gsvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.harvard.econcs.jopt.solver.mip.*;
import lombok.Getter;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.price.Prices;
import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabio Isler
 */
public final class GSVMBidder extends SATSBidder {

    private static final long serialVersionUID = -7275733600491984673L;
    private final int bidderPosition;
    private final HashMap<Long, BigDecimal> values;
    private transient GSVMWorld world;
    private final String description;

    GSVMBidder(GSVMBidderSetup setup, GSVMWorld world, int bidderPosition, long currentId, long population, RNGSupplier rngSupplier) {
        super(setup, population, currentId, world.getId());
        this.world = world;
        this.bidderPosition = bidderPosition % world.getSize();
        this.values = setup.drawValues(rngSupplier, this);
        this.description = setup.getSetupName() + " with interest in licenses "
                + this.values.keySet().stream().map(String::valueOf).collect(Collectors.joining(", "))
                + ".";
        store();
    }

    @Override
    public BigDecimal calculateValue(Bundle bundle) {
        double value = 0;
        for (Good good : bundle.getSingleQuantityGoods()) {
            GSVMLicense license = (GSVMLicense) good;
            if (this.values.containsKey(license.getLongId())) {
                value += this.values.get(license.getLongId()).doubleValue();
            }
        }
        double factor = 0;
        if (!bundle.getBundleEntries().isEmpty()) factor = 0.2 * (bundle.getBundleEntries().size() - 1);
        return BigDecimal.valueOf(value + value * factor);
    }

    public int getBidderPosition() {
        return bidderPosition;
    }

    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> clazz, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        if (clazz.isAssignableFrom(SizeBasedUniqueRandomXOR.class)) {
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
    public SATSBidder drawSimilarBidder(RNGSupplier rngSupplier) {
        return new GSVMBidder((GSVMBidderSetup) getSetup(), getWorld(), getBidderPosition(), getLongId(), getPopulation(), rngSupplier);
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

    @Override
    public List<Bundle> getBestBundles(Prices prices, int maxNumberOfBundles, boolean allowNegative, double relPoolTolerance, double absPoolTolerance, double poolTimeLimit) {
        GSVMStandardMIP mip = new GSVMStandardMIP(world, Lists.newArrayList(this), true, MipInstrumentation.MipPurpose.DEMAND_QUERY, mipInstrumentation);
        Variable priceVar = new Variable("p", VarType.DOUBLE, 0, MIP.MAX_VALUE);
        mip.getMIP().add(priceVar);
        mip.getMIP().addObjectiveTerm(-1, priceVar);
        Constraint price = new Constraint(CompareType.EQ, 0);
        price.addTerm(-1, priceVar);
        for (GSVMLicense license : world.getLicenses()) {
            Map<Integer, Variable> xVariables = mip.getXVariables(this, license);
            for (Variable xVariable : xVariables.values()) {
                price.addTerm(prices.getPrice(Bundle.of(license)).getAmount().doubleValue(), xVariable);
            }
        }
        mip.getMIP().add(price);
        List<Allocation> optimalAllocations = mip.getBestAllocations(maxNumberOfBundles, allowNegative);

        List<Bundle> result = optimalAllocations.stream()
                .map(allocation -> allocation.allocationOf(this).getBundle())
                .filter(bundle -> allowNegative || getUtility(bundle, prices).signum() > -1)
                .collect(Collectors.toList());
        if (result.isEmpty()) result.add(Bundle.EMPTY);
        return result;
    }

    @Override
    public String getDescription() {
        return description;
    }

    // region instrumentation
    @Getter
    private MipInstrumentation mipInstrumentation = new MipInstrumentation();

    @Override
    public void attachMipInstrumentation(MipInstrumentation mipInstrumentation) {
        this.mipInstrumentation = mipInstrumentation;
    }
    // endregion
}
