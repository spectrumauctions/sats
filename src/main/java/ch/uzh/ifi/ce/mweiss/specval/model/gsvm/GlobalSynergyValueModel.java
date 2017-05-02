package ch.uzh.ifi.ce.mweiss.specval.model.gsvm;

import ch.uzh.ifi.ce.mweiss.specval.model.DefaultModel;
import ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class GlobalSynergyValueModel extends DefaultModel<GSVMWorld, GSVMBidder> {
    private final GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
    private final GSVMNationalBidderSetup.Builder nationalBidderBuilder = new GSVMNationalBidderSetup.Builder();
    private final GSVMRegionalBidderSetup.Builder regionalBidderBuilder = new GSVMRegionalBidderSetup.Builder();

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.QuickDefaultAccess#createWorld(ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier)
     */
    @Override
    public GSVMWorld createWorld(RNGSupplier worldSeed) {
        return new GSVMWorld(worldSetupBuilder.build(), worldSeed);
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.QuickDefaultAccess#createPopulation(ch.uzh.ifi.ce.mweiss.specval.model.World, ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier)
     */
    @Override
    public List<GSVMBidder> createPopulation(GSVMWorld world, RNGSupplier populationRNG) {
        Collection<GSVMRegionalBidderSetup> regionalSetups = new HashSet<>();
        regionalSetups.add(regionalBidderBuilder.build());

        Collection<GSVMNationalBidderSetup> nationalSetups = new HashSet<>();
        nationalSetups.add(nationalBidderBuilder.build());

        return world.createPopulation(regionalSetups, nationalSetups, populationRNG);
    }

    public void setNumberOfNationalBidders(int numberOfBidders) {
        nationalBidderBuilder.setNumberOfBidders(numberOfBidders);
    }

    public void setNumberOfRegionalBidders(int numberOfBidders) {
        regionalBidderBuilder.setNumberOfBidders(numberOfBidders);
    }
}
