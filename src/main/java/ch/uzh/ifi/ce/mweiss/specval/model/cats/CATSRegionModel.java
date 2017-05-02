package ch.uzh.ifi.ce.mweiss.specval.model.cats;

import ch.uzh.ifi.ce.mweiss.specval.model.DefaultModel;
import ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class CATSRegionModel extends DefaultModel<CATSWorld, CATSBidder> {
    private final CATSWorldSetup.Builder worldSetupBuilder = new CATSWorldSetup.Builder();
    private final CATSBidderSetup.Builder bidderBuilder = new CATSBidderSetup.Builder();

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.QuickDefaultAccess#createWorld(ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier)
     */
    @Override
    public CATSWorld createWorld(RNGSupplier worldSeed) {
        return new CATSWorld(worldSetupBuilder.build(), worldSeed);
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.QuickDefaultAccess#createPopulation(ch.uzh.ifi.ce.mweiss.specval.model.World, ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier)
     */
    @Override
    public List<CATSBidder> createPopulation(CATSWorld world, RNGSupplier populationRNG) {
        List<CATSBidderSetup> setups = new ArrayList<>();
        setups.add(bidderBuilder.build());

        return world.createPopulation(setups, populationRNG);
    }

    public void setNumberOfBidders(int numberOfBidders) {
        bidderBuilder.setNumberOfBidders(numberOfBidders);
    }
}
