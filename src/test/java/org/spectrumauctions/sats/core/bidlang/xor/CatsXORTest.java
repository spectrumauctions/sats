package org.spectrumauctions.sats.core.bidlang.xor;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.bidder.valuefunction.BundleValue;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.cats.CATSBidder;
import org.spectrumauctions.sats.core.model.cats.CATSRegionModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CatsXORTest {

    @Test
    public void testCatsXORBids() throws UnsupportedBiddingLanguageException {
        long seed = 156567345634L;

        CATSRegionModel model = new CATSRegionModel();
        List<CATSBidder> bidders = model.createNewWorldAndPopulation(seed - 1);

        List<BundleValue> directBids = Lists.newArrayList();
        List<BundleValue> iteratorBids = Lists.newArrayList();

        for (int i = 0; i < bidders.size(); i++) {
            CatsXOR valueFunction = bidders.get(i).getValueFunction(CatsXOR.class, seed + i);

            List<BundleValue> bid = Lists.newArrayList(valueFunction.getCATSXORBids());

            if (bid.size() > 6) Assert.fail("No bid should have more than 6 bundles!");

            directBids.addAll(bid);
        }

        for (int i = 0; i < bidders.size(); i++) {
            CatsXOR valueFunction = bidders.get(i).getValueFunction(CatsXOR.class, seed + i);
            Iterator<BundleValue> catsIterator = valueFunction.iterator();

            List<BundleValue> bid = Lists.newArrayList();

            while (catsIterator.hasNext()) {
                bid.add(catsIterator.next());
            }

            if (bid.size() > 6) Assert.fail("No bid should have more than 6 bundles!");

            iteratorBids.addAll(bid);
        }

        Assert.assertTrue(directBids.size() > 0);
        Assert.assertTrue(iteratorBids.size() > 0);
        List<List<? extends Good>> iteratorBundles = iteratorBids.stream().map(bv -> bv.getBundle().getSingleQuantityGoods()).collect(Collectors.toList());
        for (BundleValue directBid : directBids) {
            Assert.assertTrue(iteratorBundles.contains(directBid.getBundle().getSingleQuantityGoods()));
        }
    }

    @Test
    public void testNoCapTooSmallWorld() throws UnsupportedBiddingLanguageException {
        long seed = 156567345634L;

        CATSRegionModel model = new CATSRegionModel();
        model.setNumberOfGoods(4);
        CATSBidder bidder = model.createNewWorldAndPopulation(seed).stream().findAny().orElseThrow(IllegalArgumentException::new);

        CatsXOR valueFunction = bidder.getValueFunction(CatsXOR.class, seed + 1).noCapForSubstitutableGoods();
        Iterator<BundleValue> catsIterator = valueFunction.iterator();

        catsIterator.next(); // Get original bundle already to test that there's no other valid bundle
        Assert.assertFalse(catsIterator.hasNext());
    }

    @Test
    public void testManyNoCap() throws UnsupportedBiddingLanguageException {
        for (long seed = 2271479L; seed < 3271479L; seed += 10000L) {
            testNoCap(seed);
        }
    }

    private void testNoCap(long seed) throws UnsupportedBiddingLanguageException {

        CATSRegionModel model = new CATSRegionModel();
        CATSBidder bidder = model.createNewWorldAndPopulation(seed).stream().findFirst().orElseThrow(IllegalArgumentException::new);

        CatsXOR valueFunction = bidder.getValueFunction(CatsXOR.class, seed + 1).noCapForSubstitutableGoods();
        Iterator<BundleValue> catsIterator = valueFunction.iterator();

        while (catsIterator.hasNext()) {
            BundleValue xor = catsIterator.next();
            Assert.assertNotNull("Null returned at seed " + seed, xor);
        }
    }

    @Test
    public void testStatisticallyAgainstOriginalOutput() throws IOException, UnsupportedBiddingLanguageException {
        Path path = Paths.get("src/test/resources/default_output_cats");
        File dir = path.toFile();

        int numberOfBids = 0;
        int numberOfBidders = 0;
        int numberOfGoods = 0;
        double valueOfGoods = 0;

        if (dir.isDirectory()) {
            for (String fileName : dir.list()) {
                try (BufferedReader br = new BufferedReader(new FileReader(path.toString() + "/" + fileName))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.length() == 0) continue;
                        if (line.substring(0, 4).equals("bids")) {
                            numberOfBids += Integer.valueOf(line.substring(5));
                        } else if (line.substring(0, 5).equals("dummy")) {
                            numberOfBidders += Integer.valueOf(line.substring(6));
                        } else {
                            String[] fields = line.split("\t");
                            try {
                                int bidNr = Integer.valueOf(fields[0]); // To check that it can be read -> we're in a bid line
                                valueOfGoods += Double.valueOf(fields[1]);
                                numberOfGoods += (fields.length - 3);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                        }
                    }

                }

            }

            double bidsPerBidderCats = (double) numberOfBids / numberOfBidders;
            double avgBundleSize = (double) numberOfGoods / numberOfBids;
            double valuePerGood = valueOfGoods / numberOfGoods;

            int numberOfBidsSats = 0;
            int numberOfBiddersSats = 0;
            int numberOfGoodsSats = 0;
            double valueOfGoodsSats = 0;

            long seed = 435987123982L;

            for (int i = 0; i < dir.list().length / 100; i++) {
                CATSRegionModel model = new CATSRegionModel();
                model.setNumberOfGoods(256);
                model.setNumberOfBidders(25);
                List<CATSBidder> bidders = model.createNewWorldAndPopulation(seed++);
                numberOfBiddersSats += bidders.size();

                for (CATSBidder bidder : bidders) {
                    CatsXOR valueFunction = bidder.getValueFunction(CatsXOR.class, seed++);
                    Set<BundleValue> bids = valueFunction.getCATSXORBids();
                    numberOfBidsSats += bids.size();
                    for (BundleValue bid : bids) {
                        numberOfGoodsSats += bid.getBundle().getSingleQuantityGoods().size();
                        valueOfGoodsSats += bid.getAmount().doubleValue();
                    }
                }
            }

            double bidsPerBidderSats = (double) numberOfBidsSats / numberOfBiddersSats;
            double avgBundleSizeSats = (double) numberOfGoodsSats / numberOfBidsSats;
            double valuePerGoodSats = valueOfGoodsSats / numberOfGoodsSats;

            // TODO: Find the reason why SATS generally has a bit lower prices, a bit less goods per bid,
            // and a bit less goods per bidder.
            Assert.assertEquals(bidsPerBidderCats, bidsPerBidderSats, 1);
            Assert.assertEquals(avgBundleSize, avgBundleSizeSats, 2);
            Assert.assertEquals(valuePerGood, valuePerGoodSats, 10);

        } else {
            Assert.fail("Directory with CATS output files not found");
        }
    }
}
