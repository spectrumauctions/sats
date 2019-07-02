package org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.bvm.bvm.BaseValueModel;
import org.spectrumauctions.sats.core.model.bvm.mbvm.MultiBandValueModel;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.core.model.srvm.SingleRegionModel;

import java.util.*;

/**
 * @author Fabio Isler
 */
public class SimpleRandomOrderTest {

    private static final Logger logger = LogManager.getLogger(SimpleRandomOrderTest.class);

    private static Map<SATSBidder, Integer> bidders;

    @BeforeClass
    public static void setUpBeforeClass() {
        bidders = new HashMap<>();
        bidders.put(new BaseValueModel().createNewPopulation().stream().findAny().orElse(null), (int) (0.8 * 140));
        bidders.put(new MultiBandValueModel().createNewPopulation().stream().findAny().orElse(null), 500);
        bidders.put(new SingleRegionModel().createNewPopulation().stream().findAny().orElse(null), 500);
        bidders.put(new MultiRegionModel().createNewPopulation().stream().findAny().orElse(null), 500);
    }

    @Test
    public void testAllModelsSimpleRandom() {
        for (Map.Entry<SATSBidder, Integer> entry : bidders.entrySet()) {
            testSimple(entry.getKey(), entry.getValue());
        }
    }

    @Test
    public void testSRMSimpleRandomReduceBidAmount() {
        SATSBidder bidder = new SingleRegionModel().createNewPopulation().stream().findAny().orElse(null);
        XORQRandomOrderSimple<?, ?> valueFunction;
        try {
            valueFunction = (XORQRandomOrderSimple) bidder.getValueFunction(XORQRandomOrderSimple.class);
            valueFunction.setIterations(5000);
            Iterator<? extends GenericValue<?, ?>> xorqBidIterator = valueFunction.iterator();
            Set<GenericValue> bids = createBids(xorqBidIterator);
            int assumedSize = (int) (0.8 * (6 * 9 * 14 + 1));
            Assert.assertTrue(bids.size() == assumedSize);
        } catch (UnsupportedBiddingLanguageException e) {
            logger.error("Unsupported bidding language!");
            Assert.fail();
        }
    }

    @Test
    public void testMRMSimpleRandomLarge() {
        SATSBidder bidder = new MultiRegionModel().createNewPopulation().stream().findAny().orElse(null);
        XORQRandomOrderSimple<?, ?> valueFunction;
        try {
            valueFunction = (XORQRandomOrderSimple) bidder.getValueFunction(XORQRandomOrderSimple.class);
            valueFunction.setIterations(5000);
            Iterator<? extends GenericValue<?, ?>> xorqBidIterator = valueFunction.iterator();
            Set<GenericValue> bids = createBids(xorqBidIterator);
            Assert.assertTrue(bids.size() == 5000);
        } catch (UnsupportedBiddingLanguageException e) {
            logger.error("Unsupported bidding language!");
            Assert.fail();
        }
    }

    // ------- Helpers ------- //

    private void testSimple(SATSBidder bidder, Integer assumedBidSize) {
        XORQRandomOrderSimple<?, ?> valueFunction;
        try {
            valueFunction = (XORQRandomOrderSimple) bidder.getValueFunction(XORQRandomOrderSimple.class);
            Iterator<? extends GenericValue<?, ?>> xorqBidIterator = valueFunction.iterator();
            Set<GenericValue> bids = createBids(xorqBidIterator);

            Assert.assertTrue(bids.size() == assumedBidSize);
        } catch (UnsupportedBiddingLanguageException e) {
            logger.error("Unsupported bidding language!");
            Assert.fail();
        }
    }

    private Set<GenericValue> createBids(Iterator<? extends GenericValue<?, ?>> xorqBidIterator) {
        Set<GenericValue> cache = new HashSet<>();
        while (xorqBidIterator.hasNext()) {
            GenericValue bid = xorqBidIterator.next();
            Assert.assertTrue("Should be a new bid", !exists(bid, cache));
            cache.add(bid);
        }
        return cache;
    }

    private boolean exists(GenericValue bid, Set<GenericValue> cache) {
        for (GenericValue val : cache) {
            if (val.equals(bid)) {
                return true;
            }
        }
        return false;
    }
}
