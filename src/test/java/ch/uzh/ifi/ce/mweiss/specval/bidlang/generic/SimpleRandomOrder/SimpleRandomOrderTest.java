package ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SimpleRandomOrder;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BaseValueModel;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.mbvm.MultiBandValueModel;
import ch.uzh.ifi.ce.mweiss.specval.model.cats.CATSRegionModel;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MultiRegionModel;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SingleRegionModel;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

/**
 * @author Fabio Isler
 */
public class SimpleRandomOrderTest {

    private static Map<Bidder, Integer> bidders;

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
        for (Map.Entry<Bidder, Integer> entry : bidders.entrySet()) {
            testSimple(entry.getKey(), entry.getValue());
        }
    }

    @Test
    public void testSRMSimpleRandomReduceBidAmount() {
        Bidder bidder = new SingleRegionModel().createNewPopulation().stream().findAny().orElse(null);
        XORQRandomOrderSimple<?> valueFunction;
        try {
            valueFunction = (XORQRandomOrderSimple) bidder.getValueFunction(XORQRandomOrderSimple.class);
            valueFunction.setIterations(5000);
            Iterator<? extends GenericValue<?>> xorqBidIterator = valueFunction.iterator();
            Set<GenericValue> bids = createBids(xorqBidIterator);
            int assumedSize = (int) (0.8 * (6 * 9 * 14 + 1));
            Assert.assertTrue(bids.size() == assumedSize);
        } catch (UnsupportedBiddingLanguageException e) {
            System.out.println("Unsupported bidding language!");
            Assert.fail();
        }
    }

    @Test
    public void testMRMSimpleRandomLarge() {
        Bidder bidder = new MultiRegionModel().createNewPopulation().stream().findAny().orElse(null);
        XORQRandomOrderSimple<?> valueFunction;
        try {
            valueFunction = (XORQRandomOrderSimple) bidder.getValueFunction(XORQRandomOrderSimple.class);
            valueFunction.setIterations(5000);
            Iterator<? extends GenericValue<?>> xorqBidIterator = valueFunction.iterator();
            Set<GenericValue> bids = createBids(xorqBidIterator);
            Assert.assertTrue(bids.size() == 5000);
        } catch (UnsupportedBiddingLanguageException e) {
            System.out.println("Unsupported bidding language!");
            Assert.fail();
        }
    }

    // ------- Helpers ------- //

    private void testSimple(Bidder bidder, Integer assumedBidSize) {
        XORQRandomOrderSimple<?> valueFunction;
        try {
            valueFunction = (XORQRandomOrderSimple) bidder.getValueFunction(XORQRandomOrderSimple.class);
            Iterator<? extends GenericValue<?>> xorqBidIterator = valueFunction.iterator();
            Set<GenericValue> bids = createBids(xorqBidIterator);

            Assert.assertTrue(bids.size() == assumedBidSize);
        } catch (UnsupportedBiddingLanguageException e) {
            System.out.println("Unsupported bidding language!");
            Assert.fail();
        }
    }

    private Set<GenericValue> createBids(Iterator<? extends GenericValue<?>> xorqBidIterator) {
        Set<GenericValue> cache = new HashSet<>();
        while (xorqBidIterator.hasNext()) {
            GenericValue bid = xorqBidIterator.next();
            Assert.assertTrue("Should be a new bid", !exists(bid, cache));
//            System.out.println(bid.getQuantities() + "    " + bid.getValue());
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
