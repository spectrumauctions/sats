package org.spectrumauctions.sats.core.bidlang.xor;

import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.cats.*;

import java.util.*;

public class CatsXORTest {

    @Test
    public void testCatsXORBids() throws UnsupportedBiddingLanguageException {
        long seed = 156567345634L;

        CATSRegionModel model = new CATSRegionModel();
        model.setNumberOfBidders(25);
        List<CATSBidder> bidders = model.createNewPopulation(seed - 1);

        int count = 0;
        for (int i = 0; i < bidders.size(); i++) {
            CatsXOR valueFunction = bidders.get(i).getValueFunction(CatsXOR.class, seed + i);
            Set<XORValue<CATSLicense>> catsBids = valueFunction.getCATSXORBids();

            if (catsBids.size() != 6) count++;
            if (catsBids.size() > 6) Assert.fail("No bid should have more than 6 bundles!");
        }

        Assert.assertEquals("Expected 10 bids to include less than 6 bundles with this seed. Actual: "
                + count, 10, count);
    }

    @Test
    public void testWithNulls() throws UnsupportedBiddingLanguageException {
        long seed = 156567345634L;

        CATSRegionModel model = new CATSRegionModel();
        model.setNumberOfBidders(25);
        model.setNumberOfGoods(4);
        List<CATSBidder> bidders = model.createNewPopulation(seed - 1);

        int count = 0;
        int nullCount = 0;
        for (int i = 0; i < bidders.size(); i++) {
            CatsXOR valueFunction = bidders.get(i).getValueFunction(CatsXOR.class, seed + i);
            Iterator<XORValue<CATSLicense>> catsIterator = valueFunction.iterator();
            Set<XORValue<CATSLicense>> bids = new HashSet<>();

            while (catsIterator.hasNext()) {
                XORValue<CATSLicense> xor = catsIterator.next();
                if (xor != null) bids.add(xor);
                else nullCount ++;
            }

            if (bids.size() == 1) count++;
        }

        Assert.assertEquals("Expected 21 bids to include only one bundle with this seed. Actual: " + count,
                21, count);
        Assert.assertEquals("Expected 12 'Null' bundles in all the bids. Actual: "
                + nullCount, 12, nullCount);
    }

    @Test
    public void testWithoutNullsTooSmallWorld() throws UnsupportedBiddingLanguageException {
        long seed = 156567345634L;

        CATSRegionModel model = new CATSRegionModel();
        model.setNumberOfGoods(4);
        CATSBidder bidder = model.createNewPopulation(seed).stream().findAny().orElseThrow(IllegalArgumentException::new);

        CatsXOR valueFunction = bidder.getValueFunction(CatsXOR.class, seed + 1);
        Iterator<XORValue<CATSLicense>> catsIterator = valueFunction.iteratorWithoutNulls();

        catsIterator.next(); // Get original bundle already to test that there's no other valid bundle

        while (catsIterator.hasNext()) {
            try {
                catsIterator.next();
                Assert.fail(); // Should never succeed to get here
            } catch (NoSuchElementException e) {
                break; // Should end up here
            }
        }


    }

    @Test
    public void testManyWithoutNulls() throws UnsupportedBiddingLanguageException {
        for (long seed = 2271479L; seed < 3271479L; seed += 10000L) {
            testWithoutNulls(seed);
        }
    }

    private void testWithoutNulls(long seed) throws UnsupportedBiddingLanguageException {

        CATSRegionModel model = new CATSRegionModel();
        CATSBidder bidder = model.createNewPopulation(seed).stream().findAny().orElseThrow(IllegalArgumentException::new);

        CatsXOR valueFunction = bidder.getValueFunction(CatsXOR.class, seed + 1);
        Iterator<XORValue<CATSLicense>> catsIterator = valueFunction.iteratorWithoutNulls();

        while (catsIterator.hasNext()) {
            try {
                XORValue<CATSLicense> xor = catsIterator.next();
                Assert.assertNotNull(xor);
            } catch (NoSuchElementException e) {
                Assert.fail("At seed " + seed + ": " + e.getMessage());
            }
        }
    }
}
