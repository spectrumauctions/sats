package org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.bidder.valuefunction.BundleValue;
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
        bidders.put(new BaseValueModel().createNewWorldAndPopulation().stream().findAny().orElse(null), (int) (0.8 * 140));
        bidders.put(new MultiBandValueModel().createNewWorldAndPopulation().stream().findAny().orElse(null), 500);
        bidders.put(new SingleRegionModel().createNewWorldAndPopulation().stream().findAny().orElse(null), 500);
        bidders.put(new MultiRegionModel().createNewWorldAndPopulation().stream().findAny().orElse(null), 500);
    }

    @Test
    public void testAllModelsSimpleRandom() {
        for (Map.Entry<SATSBidder, Integer> entry : bidders.entrySet()) {
            testSimple(entry.getKey(), entry.getValue());
        }
    }

    @Test
    public void testSRMSimpleRandomReduceBidAmount() {
        SATSBidder bidder = new SingleRegionModel().createNewWorldAndPopulation().stream().findAny().orElseThrow(NoSuchElementException::new);
        XORQRandomOrderSimple valueFunction;
        try {
            valueFunction = bidder.getValueFunction(XORQRandomOrderSimple.class);
            valueFunction.setIterations(5000);
            Iterator<BundleValue> xorqBidIterator = valueFunction.iterator();
            Set<BundleValue> bids = createBids(xorqBidIterator);
            int assumedSize = (int) (0.8 * (6 * 9 * 14 + 1));
            Assert.assertEquals(bids.size(), assumedSize);
        } catch (UnsupportedBiddingLanguageException e) {
            logger.error("Unsupported bidding language!");
            Assert.fail();
        }
    }

    @Test
    public void testMRMSimpleRandomLarge() {
        SATSBidder bidder = new MultiRegionModel().createNewWorldAndPopulation().stream().findAny().orElseThrow(NoSuchElementException::new);
        XORQRandomOrderSimple valueFunction;
        try {
            valueFunction = bidder.getValueFunction(XORQRandomOrderSimple.class);
            valueFunction.setIterations(5000);
            Iterator<BundleValue> xorqBidIterator = valueFunction.iterator();
            Set<BundleValue> bids = createBids(xorqBidIterator);
            Assert.assertEquals(5000, bids.size());
        } catch (UnsupportedBiddingLanguageException e) {
            logger.error("Unsupported bidding language!");
            Assert.fail();
        }
    }

    // ------- Helpers ------- //

    private void testSimple(SATSBidder bidder, Integer assumedBidSize) {
        XORQRandomOrderSimple valueFunction;
        try {
            valueFunction = bidder.getValueFunction(XORQRandomOrderSimple.class);
            Iterator<BundleValue> xorqBidIterator = valueFunction.iterator();
            Set<BundleValue> bids = createBids(xorqBidIterator);

            Assert.assertTrue(bids.size() == assumedBidSize);
        } catch (UnsupportedBiddingLanguageException e) {
            logger.error("Unsupported bidding language!");
            Assert.fail();
        }
    }

    private Set<BundleValue> createBids(Iterator<BundleValue> xorqBidIterator) {
        Set<BundleValue> cache = new HashSet<>();
        while (xorqBidIterator.hasNext()) {
            BundleValue bid = xorqBidIterator.next();
            Assert.assertFalse(exists(bid, cache));
            cache.add(bid);
        }
        return cache;
    }

    private boolean exists(BundleValue bid, Set<BundleValue> cache) {
        for (BundleValue val : cache) {
            if (val.equals(bid)) {
                return true;
            }
        }
        return false;
    }
}
