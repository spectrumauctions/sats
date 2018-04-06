package org.spectrumauctions.sats.mechanism;

import org.mockito.Mockito;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.*;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("serial") //No Serialization
public class MockWorld extends World {

    int numberOfGoods;
    int numberOfBidders;

    static BidderSetup setup = Mockito.mock(BidderSetup.class);

    private static MockWorld INSTANCE = null;

    public static MockWorld getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MockWorld();
        }
        return INSTANCE;
    }

    public void reset() {
        numberOfGoods = 0;
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

    public MockBidder createNewBidder() {
        return new MockBidder(0, numberOfBidders++, this.getId());
    }

    @Override
    public Set<? extends Good> getLicenses() {
        throw new UnsupportedOperationException("Not implemented for MockWorld");
    }

    @Override
    public Collection<? extends Bidder<?>> restorePopulation(long populationId) {
        throw new UnsupportedOperationException("Not implemented for MockWorld");
    }

    @Override
    public void refreshFieldBackReferences() {
    }

    public class MockGood extends Good {
        protected MockGood(long id, long worldId) {
            super(id, worldId);
        }

        @Override
        public World getWorld() {
            return MockWorld.this;
        }
    }

    public class MockBidder extends Bidder<MockGood> {

        Set<XORValue<MockGood>> bids = new HashSet<>();

        public void addBid(Bundle<MockGood> bundle, double value) {
            bids.add(new XORValue<>(bundle, BigDecimal.valueOf(value)));
        }

        public Set<XORValue<MockGood>> getBids() {
            return bids;
        }

        protected MockBidder(long population, long id, long worldId) {
            super(setup, population, id, worldId);
        }

        @Override
        public BigDecimal calculateValue(Bundle<MockGood> bundle) {
            Optional<XORValue<MockGood>> value = bids.stream().filter(bid -> bid.getLicenses().equals(bundle)).findFirst();
            if (value.isPresent()) return value.get().value();
            else return BigDecimal.ZERO;
        }

        @Override
        public <T extends BiddingLanguage> T getValueFunction(Class<T> type, long seed)
                throws UnsupportedBiddingLanguageException {
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

    }


}
