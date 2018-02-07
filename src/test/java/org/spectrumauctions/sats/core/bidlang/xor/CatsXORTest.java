package org.spectrumauctions.sats.core.bidlang.xor;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.cats.*;

import java.util.*;
import java.util.stream.Collectors;

public class CatsXORTest {

    @Test
    public void testCatsXORBids() throws UnsupportedBiddingLanguageException {
        long seed = 156567345634L;

        CATSRegionModel model = new CATSRegionModel();
        List<CATSBidder> bidders = model.createNewPopulation(seed - 1);

        List<XORValue<CATSLicense>> directBids = Lists.newArrayList();
        List<XORValue<CATSLicense>> iteratorBids = Lists.newArrayList();

        for (int i = 0; i < bidders.size(); i++) {
            CatsXOR valueFunction = bidders.get(i).getValueFunction(CatsXOR.class, seed + i);

            List<XORValue<CATSLicense>> bid = Lists.newArrayList(valueFunction.getCATSXORBids());

            if (bid.size() > 6) Assert.fail("No bid should have more than 6 bundles!");

            directBids.addAll(bid);
        }

        for (int i = 0; i < bidders.size(); i++) {
            CatsXOR valueFunction = bidders.get(i).getValueFunction(CatsXOR.class, seed + i);
            Iterator<XORValue<CATSLicense>> catsIterator = valueFunction.iterator();

            List<XORValue<CATSLicense>> bid = Lists.newArrayList();

            while (catsIterator.hasNext()) {
                bid.add(catsIterator.next());
            }

            if (bid.size() > 6) Assert.fail("No bid should have more than 6 bundles!");

            iteratorBids.addAll(bid);
        }

        Assert.assertTrue(directBids.size() > 0);
        Assert.assertTrue(iteratorBids.size() > 0);
        Collection<Bundle<CATSLicense>> iteratorBundles = iteratorBids.stream().map(XORValue::getLicenses).collect(Collectors.toList());
        for (int i = 0; i < directBids.size(); i++) {
            Assert.assertTrue(iteratorBundles.contains(directBids.get(i).getLicenses()));
        }
    }

    @Test
    public void testNoCapTooSmallWorld() throws UnsupportedBiddingLanguageException {
        long seed = 156567345634L;

        CATSRegionModel model = new CATSRegionModel();
        model.setNumberOfGoods(4);
        CATSBidder bidder = model.createNewPopulation(seed).stream().findAny().orElseThrow(IllegalArgumentException::new);

        CatsXOR valueFunction = bidder.getValueFunction(CatsXOR.class, seed + 1).noCapForSubstitutableGoods();
        Iterator<XORValue<CATSLicense>> catsIterator = valueFunction.iterator();

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
    public void testManyNoCap() throws UnsupportedBiddingLanguageException {
        for (long seed = 2271479L; seed < 3271479L; seed += 10000L) {
            testNoCap(seed);
        }
    }

    private void testNoCap(long seed) throws UnsupportedBiddingLanguageException {

        CATSRegionModel model = new CATSRegionModel();
        CATSBidder bidder = model.createNewPopulation(seed).stream().findAny().orElseThrow(IllegalArgumentException::new);

        CatsXOR valueFunction = bidder.getValueFunction(CatsXOR.class, seed + 1).noCapForSubstitutableGoods();
        Iterator<XORValue<CATSLicense>> catsIterator = valueFunction.iterator();

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
