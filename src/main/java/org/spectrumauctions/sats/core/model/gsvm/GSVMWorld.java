package org.spectrumauctions.sats.core.model.gsvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Fabio Isler
 */
public final class GSVMWorld extends World {
    private static final long serialVersionUID = 1737956952755986936L;
    private static final String MODEL_NAME = "Global Synergy Value Model";

    private final Integer size;

    private final GSVMCircle nationalCircle;
    private final GSVMCircle regionalCircle;
    private transient ImmutableList<GSVMLicense> licenseList;
    /**
     *  In earlier versions of SATS (earlier than 0.7.0), the original model was interpreted differently than it is today.
     *  Back then, when asking a bidder what her value is for bundle X, the synergy factor increased with any good in X.
     *  Now, the synergy factor only increases with goods which the bidder has a positive value for.
     *  This flag can be set to true in order to reproduce results of the old SATS versions.
     */
    @Getter
    private final boolean isLegacyGSVM;

    public GSVMWorld(GSVMWorldSetup worldSetup, RNGSupplier rngSupplier) {
        super(MODEL_NAME);
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        this.size = worldSetup.drawSize(rng);
        this.nationalCircle = new GSVMCircle(this, size * 2, 0);
        this.regionalCircle = new GSVMCircle(this, size, size * 2);
        this.isLegacyGSVM = worldSetup.isLegacyGSVM();
        store();
    }

    public int getSize() {
        return size;
    }

    public GSVMCircle getNationalCircle() {
        return nationalCircle;
    }

    public GSVMCircle getRegionalCircle() {
        return regionalCircle;
    }

    @Override
    public List<GSVMBidder> restorePopulation(long populationId) {
        return super.restorePopulation(GSVMBidder.class, populationId);
    }

    /**
     * {@inheritDoc}
     *
     * @return An immutable set containing all licenses.
     */
    @Override
    public ImmutableList<GSVMLicense> getLicenses() {
        if (licenseList == null) {
            ImmutableList.Builder<GSVMLicense> builder = ImmutableList.builder();
            builder.add(nationalCircle.getLicenses());
            builder.add(regionalCircle.getLicenses());
            licenseList = builder.build();
        }
        return licenseList;
    }

    /* (non-Javadoc)
     * @see World#getNumberOfGoods()
     */
    @Override
    public int getNumberOfGoods() {
        return nationalCircle.getLicenses().length + regionalCircle.getLicenses().length;
    }

    /* (non-Javadoc)
     * @see World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        nationalCircle.refreshFieldBackReferences(this);
        regionalCircle.refreshFieldBackReferences(this);
    }

    public List<GSVMBidder> createPopulation(Collection<GSVMRegionalBidderSetup> regionalSetups,
                                             Collection<GSVMNationalBidderSetup> nationalSetups,
                                             RNGSupplier populationRNG) {
        long population = openNewPopulation();
        long currentId = 0;
        List<GSVMBidder> bidders = new ArrayList<>();
        if (regionalSetups != null) {
            int position = 0;
            for (GSVMRegionalBidderSetup setup : regionalSetups) {
                for (int i = 0; i < setup.getNumberOfBidders(); i++) {
                    bidders.add(new GSVMBidder(setup, this, position++, currentId++, population, populationRNG));
                }
            }
        }
        if (nationalSetups != null) {
            for (GSVMBidderSetup setup : nationalSetups) {
                for (int i = 0; i < setup.getNumberOfBidders(); i++) {
                    bidders.add(new GSVMBidder(setup, this, -1, currentId++, population, populationRNG));
                }
            }
        }
        Preconditions.checkArgument(bidders.size() > 0, "At least one bidder setup with a strictly positive number of bidders is required to generate population");
        return bidders;
    }

}
