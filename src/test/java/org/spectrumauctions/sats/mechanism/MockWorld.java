package org.spectrumauctions.sats.mechanism;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import org.mockito.Mockito;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.generic.Band;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.*;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

@SuppressWarnings("serial") //No Serialization
public class MockWorld extends World implements GenericWorld<MockWorld.MockGood> {

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

    public MockBand createNewBand(Set<MockGood> goods) {
        return new MockBand(numberOfBands++, goods);
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

    @Override
    public Set<GenericDefinition<MockGood>> getAllGenericDefinitions() {
        throw new UnsupportedOperationException("Not implemented for MockWorld");
    }

    @Override
    public GenericDefinition<MockGood> getGenericDefinitionOf(MockGood good) {
        throw new UnsupportedOperationException("Not implemented for MockWorld");
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

    public class MockBand extends Band implements GenericDefinition<MockGood> {
        private Set<MockGood> goods;
        protected MockBand(int id, Set<MockGood> goods) {
            super(Integer.toString(id));
            this.goods = goods;
        }

        @Override
        public int getNumberOfLicenses() {
            return goods.size();
        }

        @Override
        public boolean isPartOf(MockGood good) {
            return goods.contains(good);
        }

        @Override
        public int numberOfLicenses() {
            return goods.size();
        }

        @Override
        public Set<MockGood> allLicenses() {
            return goods;
        }

        @Override
        public JsonElement shortJson() {
            throw new UnsupportedOperationException("Not supported in mock");
        }
    }


    public class MockBidder extends Bidder<MockGood> implements GenericValueBidder<MockBand> {

        Set<XORValue<MockGood>> bids = new HashSet<>();
        List<GenericValue<GenericDefinition<MockGood>, MockGood>> genericBids = new ArrayList<>();

        public void addBid(Bundle<MockGood> bundle, double value) {
            bids.add(new XORValue<>(bundle, BigDecimal.valueOf(value)));
        }
        public void addGenericBid(Map<MockBand, Integer> quantities, double value) {
            GenericValue.Builder<GenericDefinition<MockGood>, MockGood> builder = new GenericValue.Builder<>(BigDecimal.valueOf(value));
            for (Map.Entry<MockBand, Integer> entry : quantities.entrySet()) {
                builder.putQuantity(entry.getKey(), entry.getValue());
            }
            genericBids.add(builder.build());
        }

        public Set<XORValue<MockGood>> getBids() {
            return bids;
        }

        public List<GenericValue<GenericDefinition<MockGood>, MockGood>> getGenericBids() {
            return genericBids;
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
        public <T extends BiddingLanguage> T getValueFunction(Class<T> type, long seed) {
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
        public BigDecimal calculateValue(Map<MockBand, Integer> genericQuantities) {
            throw new UnsupportedOperationException("Not supported in mock");
        }
    }


}
