package org.spectrumauctions.sats.core.model.cats;

import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class CATSRegionModel extends DefaultModel<CATSWorld, CATSBidder> {
    private final CATSWorldSetup.Builder worldSetupBuilder = new CATSWorldSetup.Builder();
    private final CATSBidderSetup.Builder bidderBuilder = new CATSBidderSetup.Builder();

    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createWorld(RNGSupplier)
     */
    @Override
    public CATSWorld createWorld(RNGSupplier worldSeed) {
        return new CATSWorld(worldSetupBuilder.build(), worldSeed);
    }

    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createPopulation(World, RNGSupplier)
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

    public void setNumberOfGoods(int numberOfGoods) {
        worldSetupBuilder.setNumberOfGoodsInterval(new IntegerInterval(numberOfGoods));
    }
}
