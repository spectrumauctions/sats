package org.spectrumauctions.sats.opt.model.mrvm;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.Good;
import org.marketdesignresearch.mechlib.domain.price.LinearPrices;
import org.marketdesignresearch.mechlib.domain.price.Price;
import org.marketdesignresearch.mechlib.domain.price.Prices;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.util.math.LinearFunction;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabio Isler
 */
public class MRVMDemandQueryTest {

    private static final Logger logger = LogManager.getLogger(MRVMDemandQueryTest.class);

    private MRVMWorld minimalWorld;

    @Before
    public void setUp() {
        MRVMWorldSetup.MRVMWorldSetupBuilder worldSetupBuilder = new MRVMWorldSetup.MRVMWorldSetupBuilder();
        Set<String> oldBands = new HashSet<>(worldSetupBuilder.bandSetups().keySet());

        oldBands.forEach(worldSetupBuilder::removeBandSetup);

        MRVMWorldSetup.BandSetup bandSetup = new MRVMWorldSetup.BandSetup(
                "A",
                new IntegerInterval(1),
                new DoubleInterval(20),
                new LinearFunction(BigDecimal.ONE, BigDecimal.ZERO));
        worldSetupBuilder.putBandSetup(bandSetup);

        minimalWorld = new MRVMWorld(worldSetupBuilder.build(), new JavaUtilRNGSupplier(103103));
    }

    @Test
    public void testAllBiddersInStandardModel() {
        List<MRVMBidder> bidders = new MultiRegionModel().createNewPopulation(new JavaUtilRNGSupplier(73246104));
        MRVMWorld world = bidders.iterator().next().getWorld();
        Map<Good, Price> prices = new HashMap<>();
        world.getAllGenericDefinitions().forEach(def -> prices.put(def, Price.of(1000000)));

        for (MRVMBidder bidder : bidders) {
            Bundle bundle = bidder.getBestBundle(new LinearPrices(prices));
            Assert.assertTrue(bidder.getValue(bundle).doubleValue() > 0);
            logger.info(bundle);
        }
    }

    @Test
    public void testSingleBidderResultPool() {
        List<MRVMBidder> bidders = new MultiRegionModel().createNewPopulation(new JavaUtilRNGSupplier(73246104));
        MRVMBidder bidder = bidders.get(bidders.size() - 1);
        MRVMWorld world = bidders.iterator().next().getWorld();
        Map<Good, Price> priceMap = new HashMap<>();
        world.getAllGenericDefinitions().forEach(def -> priceMap.put(def, Price.of(1000000)));
        Prices prices = new LinearPrices(priceMap);

        List<Bundle> resultSet = bidder.getBestBundles(prices, 10);
        double firstValue = bidder.getValue(resultSet.get(0)).doubleValue();
        for (Bundle result : resultSet) {
            double value = bidder.getUtility(result, prices).doubleValue();
            Assert.assertTrue(value > 0);
            Assert.assertTrue(value <= firstValue);
            Assert.assertTrue(value > firstValue / 2);
            Set<Bundle> others = resultSet.stream().filter(entry -> !entry.equals(result)).collect(Collectors.toSet());
            Assert.assertEquals(resultSet.size() - 1, others.size());
            others.forEach(res -> Assert.assertNotEquals(result, res));
        }
    }

    @Test
    public void minimalWorldLocalBidderDemandQueryTest() {

        Set<MRVMRegionsMap.Region> regions = minimalWorld.getRegionsMap().getRegions();
        Set<MRVMLocalBidderSetup> localBidderSetups = new HashSet<>();
        for (MRVMRegionsMap.Region region : regions) {
            MRVMLocalBidderSetup.Builder localBuilder = new MRVMLocalBidderSetup.Builder();
            localBuilder.setNumberOfBidders(1);
            localBuilder.setPredefinedRegionsOfInterest(Lists.newArrayList(region.getNote()));
            localBidderSetups.add(localBuilder.build());
        }

        List<MRVMBidder> bidders = minimalWorld.createPopulation(localBidderSetups, new HashSet<>(), new HashSet<>(), new JavaUtilRNGSupplier(654798));

        Map<Good, Price> priceMap = new HashMap<>();
        minimalWorld.getAllGenericDefinitions().forEach(def -> priceMap.put(def, Price.of(10000)));
        Prices prices = new LinearPrices(priceMap);


        Bundle result = bidders.get(0).getBestBundle(prices);
        logger.info(result);
        // Make sure a local bidder only picks his region
        Assert.assertEquals(1, result.getBundleEntries().size());

    }

    @Test
    public void minimalWorldRegionalBidderDemandQueryTest() {

        Set<MRVMRegionsMap.Region> regions = minimalWorld.getRegionsMap().getRegions();
        Set<MRVMRegionalBidderSetup> regionalBidderSetups = new HashSet<>();
        for (MRVMRegionsMap.Region region : regions) {
            MRVMRegionalBidderSetup.Builder regionalBuilder = new MRVMRegionalBidderSetup.Builder();
            regionalBuilder.setNumberOfBidders(1);
            regionalBuilder.setPredefinedHome(region);
            regionalBuilder.setGammaShape(2, 1e50);
            regionalBidderSetups.add(regionalBuilder.build());
        }

        List<MRVMBidder> bidders = minimalWorld.createPopulation(new HashSet<>(), regionalBidderSetups, new HashSet<>(), new JavaUtilRNGSupplier(654798));

        Map<Good, Price> priceMap = new HashMap<>();
        minimalWorld.getAllGenericDefinitions().forEach(def -> priceMap.put(def, Price.of(10000)));
        Prices prices = new LinearPrices(priceMap);

        //Map<MRVMBidder, Bundle> resultMap = new HashMap<>();
        //Map<MRVMGenericDefinition, Integer> map = new HashMap<>();
        for (MRVMBidder bidder : bidders) {
            Bundle result = bidder.getBestBundle(prices);
            //for (Map.Entry<MRVMGenericDefinition, Integer> entry : result.getResultingBundle().getQuantities().entrySet()) {
            //    if (!map.containsKey(entry.getKey())) {
            //        map.put(entry.getKey(), 0);
            //    }
            //    map.merge(entry.getKey(), entry.getValue(), Integer::sum);
            //}
            logger.info("Bidder {} chose {}.", bidder.getName(), result);
            //resultMap.put(bidder, result);
            //// Make sure a local bidder only picks his region
            //Assert.assertEquals(1, result.getResultingBundle().getSize());
        }

        //for (int picks : map.values()) {
        //    // Make sure every region is only picked once
        //    Assert.assertEquals(1, picks);
        //}

    }

    @Test
    public void minimalWorldNationalBidderDemandQueryTest() {

        Set<MRVMNationalBidderSetup> nationalBidderSetups = new HashSet<>();

        MRVMNationalBidderSetup.Builder nationalBuilder = new MRVMNationalBidderSetup.Builder();
        nationalBuilder.setNumberOfBidders(1);
        nationalBidderSetups.add(nationalBuilder.build());

        MRVMWorld world = new MultiRegionModel().createWorld(new JavaUtilRNGSupplier(74563245));
        MRVMBidder bidder = world.createPopulation(new HashSet<>(), new HashSet<>(), nationalBidderSetups,  new JavaUtilRNGSupplier(654798)).iterator().next();
        Map<Good, Price> prices = new HashMap<>();
        world.getAllGenericDefinitions().forEach(def -> prices.put(def, Price.ZERO));
        MRVMRegionsMap.Region region = world.getRegionsMap().getRegions().stream().findAny().orElseThrow(NoSuchElementException::new);
        Set<MRVMGenericDefinition> genericDefinitions = world.getAllGenericDefinitions().stream().filter(def -> def.getRegion().equals(region)).collect(Collectors.toSet());

        // Assert that the bidder doesn't choose licenses from a region because it's too expensive
        genericDefinitions.forEach(def -> prices.put(def, Price.of(1000000000)));
        Bundle result = bidder.getBestBundle(new LinearPrices(prices));
        Set<MRVMRegionsMap.Region> regionsCovered = result.getBundleEntries().stream().map(be -> ((MRVMGenericDefinition) be.getGood()).getRegion()).collect(Collectors.toSet());
        Assert.assertEquals(regionsCovered.size(), world.getRegionsMap().getNumberOfRegions() - 1);

        // Assert that the bidder still chooses the licenses because the prices are less than the discount for losing a region
        genericDefinitions.forEach(l -> prices.put(l, Price.of(1000)));
        result = bidder.getBestBundle(new LinearPrices(prices));
        Set<MRVMRegionsMap.Region> regionsCovered2 = result.getBundleEntries().stream().map(be -> ((MRVMGenericDefinition) be.getGood()).getRegion()).collect(Collectors.toSet());
        Assert.assertEquals(regionsCovered2.size(), world.getRegionsMap().getNumberOfRegions());

    }
}
