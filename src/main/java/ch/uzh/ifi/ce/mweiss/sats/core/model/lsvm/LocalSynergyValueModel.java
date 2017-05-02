package ch.uzh.ifi.ce.mweiss.sats.core.model.lsvm;

import ch.uzh.ifi.ce.mweiss.sats.core.model.DefaultModel;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabio Isler
 *
 */
public class LocalSynergyValueModel extends DefaultModel<LSVMWorld, LSVMBidder> {

    private final LSVMWorldSetup.LSVMWorldSetupBuilder worldSetupBuilder = new LSVMWorldSetup.LSVMWorldSetupBuilder();
    private final LSVMBidderSetup.NationalBidderBuilder nationalBidderBuilder = new LSVMBidderSetup.NationalBidderBuilder();
    private final LSVMBidderSetup.RegionalBidderBuilder regionalBidderBuilder = new LSVMBidderSetup.RegionalBidderBuilder();

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.satscore.model.QuickDefaultAccess#createWorld(RNGSupplier)
     */
    @Override
    public LSVMWorld createWorld(RNGSupplier worldSeed) {
        return new LSVMWorld(worldSetupBuilder.build(), worldSeed);
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.satscore.model.QuickDefaultAccess#createPopulation(World, RNGSupplier)
     */
    @Override
    public List<LSVMBidder> createPopulation(LSVMWorld world, RNGSupplier populationRNG) {
        List<LSVMBidderSetup> setups = new ArrayList<>();
        setups.add(nationalBidderBuilder.build());
        setups.add(regionalBidderBuilder.build());
        return world.createPopulation(setups, populationRNG);
    }

    public void setNumberOfNationalBidders(int numberOfBidders) {
        nationalBidderBuilder.setNumberOfBidders(numberOfBidders);
    }

    public void setNumberOfRegionalBidders(int numberOfBidders) {
        regionalBidderBuilder.setNumberOfBidders(numberOfBidders);
    }
}
