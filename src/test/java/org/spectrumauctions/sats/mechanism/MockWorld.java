package org.spectrumauctions.sats.mechanism;

import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.bidder.value.BundleValue;
import org.marketdesignresearch.mechlib.domain.bidder.value.XORValue;
import org.marketdesignresearch.mechlib.domain.price.Prices;
import org.mockito.Mockito;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.*;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("serial") //No Serialization
public class MockWorld extends World implements GenericWorld {

    private int numberOfGoods;
    private int numberOfBands;
    private int numberOfBidders;

    private static BidderSetup setup = Mockito.mock(BidderSetup.class);

    private static MockWorld INSTANCE = null;

    public static MockWorld getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MockWorld();
        }
        return INSTANCE;
    }

    public void reset() {
        numberOfGoods = 0;
        numberOfBands = 0;
        numberOfBidders = 0;
    }

    private MockWorld() {
        super("mockWorld");
        Mockito.when(setup.getSetupName()).thenReturn("Mock");
        reset();
    }

    @Override
    public int getNumberOfGoods() {
        return numberOfGoods;
    }

    public MockGood createNewGood() {
        return new MockGood(numberOfGoods++, this.getId());
    }

    public MockBand createNewBand(List<MockGood> goods) {
        return new MockBand(numberOfBands++, goods);
    }

    public MockBidder createNewBidder() {
        return new MockBidder(0, numberOfBidders++, this.getId());
    }

    @Override
    public List<? extends License> getLicenses() {
        throw new UnsupportedOperationException("Not implemented for MockWorld");
    }

    @Override
    public Collection<? extends SATSBidder> restorePopulation(long populationId) {
        throw new UnsupportedOperationException("Not implemented for MockWorld");
    }

    @Override
    public void refreshFieldBackReferences() {
    }

    @Override
    public List<GenericGood> getAllGenericDefinitions() {
        throw new UnsupportedOperationException("Not implemented for MockWorld");
    }

    public class MockGood extends License {
        protected MockGood(long id, long worldId) {
            super(id, worldId);
        }

        @Override
        public World getWorld() {
            return MockWorld.this;
        }
    }

    public class MockBand extends GenericGood {
        private List<MockGood> goods;

        protected MockBand(int id, List<MockGood> goods) {
            super(Integer.toString(id), goods.iterator().next().getWorldId());
            this.goods = goods;
        }

        @Override
        public int available() {
            return goods.size();
        }

        @Override
        public World getWorld() {
            return goods.iterator().next().getWorld();
        }

        @Override
        public List<MockGood> containedGoods() {
            return goods;
        }
    }


    public class MockBidder extends SATSBidder {

        XORValue values = new XORValue();

        public void addBid(Bundle bundle, double value) {
            values.addBundleValue(new BundleValue(BigDecimal.valueOf(value), bundle));
        }

        public XORValue getValues() {
            return values;
        }

        protected MockBidder(long population, long id, long worldId) {
            super(setup, population, id, worldId);
        }

        @Override
        public BigDecimal calculateValue(Bundle bundle) {
            return values.getValueFor(bundle);
        }

        @Override
        public <T extends BiddingLanguage> T getValueFunction(Class<T> type, RNGSupplier rngSupplier) {
            throw new UnsupportedOperationException("Not supported in mock");
        }

        @Override
        public World getWorld() {
            return MockWorld.this;
        }

        @Override
        public void refreshReference(World world) {
            throw new UnsupportedOperationException("Not supported in mock");
        }

        @Override
        public SATSBidder drawSimilarBidder(RNGSupplier rngSupplier) {
            throw new UnsupportedOperationException("Not supported in mock");
        }

        @Override
        public List<Bundle> getBestBundles(Prices prices, int maxNumberOfBundles, boolean allowNegative) {
            return values.getOptimalBundleValueAt(prices, maxNumberOfBundles).stream().map(BundleValue::getBundle).collect(Collectors.toList());
        }
    }


}
