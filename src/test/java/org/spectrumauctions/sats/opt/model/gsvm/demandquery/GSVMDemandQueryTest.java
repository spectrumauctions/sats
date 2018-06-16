package org.spectrumauctions.sats.opt.model.gsvm.demandquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.gsvm.GSVMWorld;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public class GSVMDemandQueryTest {

    private static final Logger logger = LogManager.getLogger(GSVMDemandQueryTest.class);

    private GSVMWorld minimalWorld;


    @Test
    public void testAllBiddersInGSVM() {
        List<GSVMBidder> bidders = new GlobalSynergyValueModel().createNewPopulation(new JavaUtilRNGSupplier(73246104));
        GSVMWorld world = bidders.iterator().next().getWorld();
        Map<GSVMLicense, BigDecimal> prices = new HashMap<>();
        world.getLicenses().forEach(license -> prices.put(license, BigDecimal.valueOf(0)));

        for (GSVMBidder bidder : bidders) {
            GSVM_DemandQueryMIP mip = new GSVM_DemandQueryMIP(bidder, prices);
            GSVM_DemandQueryMipResult result = mip.getResult();
            Assert.assertTrue(result.getResultingBundle().value().doubleValue() > 0);
            logger.info(result.getResultingBundle());
        }
    }
//
//    @Test
//    public void testSingleBidderResultPool() {
//        List<GSVMBidder> bidders = new MultiRegionModel().createNewPopulation(new JavaUtilRNGSupplier(73246104));
//        GSVMBidder bidder = bidders.get(bidders.size() - 1);
//        GSVMWorld world = bidders.iterator().next().getWorld();
//        Map<GSVMGenericDefinition, BigDecimal> prices = new HashMap<>();
//        world.getAllGenericDefinitions().forEach(def -> prices.put((GSVMGenericDefinition) def, BigDecimal.valueOf(1000000)));
//
//        GSVM_DemandQueryMIP mip = new GSVM_DemandQueryMIP(bidder, prices);
//        Set<GSVMDemandQueryMipResult> resultSet = mip.getResultPool(100);
//        for (GSVMDemandQueryMipResult result : resultSet) {
//            Assert.assertTrue(result.getResultingBundle().getValue().doubleValue() > 0);
//            logger.info(result.getResultingBundle());
//            Set<GSVMDemandQueryMipResult> others = resultSet.stream().filter(entry -> !entry.equals(result)).collect(Collectors.toSet());
//            Assert.assertEquals(resultSet.size() - 1, others.size());
//            others.forEach(res -> Assert.assertNotEquals(result.getResultingBundle(), res.getResultingBundle()));
//        }
//    }
//
//    @Test
//    public void minimalWorldLocalBidderDemandQueryTest() {
//
//        Set<GSVMRegionsMap.Region> regions = minimalWorld.getRegionsMap().getRegions();
//        Set<GSVMLocalBidderSetup> localBidderSetups = new HashSet<>();
//        for (GSVMRegionsMap.Region region : regions) {
//            GSVMLocalBidderSetup.Builder localBuilder = new GSVMLocalBidderSetup.Builder();
//            localBuilder.setNumberOfBidders(1);
//            localBuilder.setPredefinedRegionsOfInterest(Lists.newArrayList(region.getNote()));
//            localBidderSetups.add(localBuilder.build());
//        }
//
//        List<GSVMBidder> bidders = minimalWorld.createPopulation(localBidderSetups, new HashSet<>(), new HashSet<>(), new JavaUtilRNGSupplier(654798));
//
//        Map<GSVMGenericDefinition, BigDecimal> prices = new HashMap<>();
//        minimalWorld.getAllGenericDefinitions().forEach(def -> prices.put((GSVMGenericDefinition) def, BigDecimal.valueOf(10000)));
//
//        Map<GSVMBidder, GenericValue<GSVMGenericDefinition, GSVMLicense>> resultMap = new HashMap<>();
//        Map<GSVMGenericDefinition, Integer> map = new HashMap<>();
//        for (GSVMBidder bidder : bidders) {
//            GSVM_DemandQueryMIP mip = new GSVM_DemandQueryMIP(bidder, prices);
//            GSVMDemandQueryMipResult result = mip.getResult();
//            for (Map.Entry<GSVMGenericDefinition, Integer> entry : result.getResultingBundle().getQuantities().entrySet()) {
//                if (!map.containsKey(entry.getKey())) {
//                    map.put(entry.getKey(), 0);
//                }
//                map.merge(entry.getKey(), entry.getValue(), Integer::sum);
//            }
//            logger.info(result.getResultingBundle());
//            resultMap.put(bidder, result.getResultingBundle());
//            // Make sure a local bidder only picks his region
//            Assert.assertEquals(1, result.getResultingBundle().getSize());
//        }
//
//        for (int picks : map.values()) {
//            // Make sure every region is only picked once
//            Assert.assertEquals(1, picks);
//        }
//
//    }
//
//    @Test
//    public void minimalWorldRegionalBidderDemandQueryTest() {
//
//        Set<GSVMRegionsMap.Region> regions = minimalWorld.getRegionsMap().getRegions();
//        Set<GSVMRegionalBidderSetup> regionalBidderSetups = new HashSet<>();
//        for (GSVMRegionsMap.Region region : regions) {
//            GSVMRegionalBidderSetup.Builder regionalBuilder = new GSVMRegionalBidderSetup.Builder();
//            regionalBuilder.setNumberOfBidders(1);
//            regionalBuilder.setPredefinedHome(region);
//            regionalBuilder.setGammaShape(2, 1e50);
//            regionalBidderSetups.add(regionalBuilder.build());
//        }
//
//        List<GSVMBidder> bidders = minimalWorld.createPopulation(new HashSet<>(), regionalBidderSetups, new HashSet<>(), new JavaUtilRNGSupplier(654798));
//
//        Map<GSVMGenericDefinition, BigDecimal> prices = new HashMap<>();
//        minimalWorld.getAllGenericDefinitions().forEach(def -> prices.put((GSVMGenericDefinition) def, BigDecimal.valueOf(10000)));
//
//        Map<GSVMBidder, GenericValue<GSVMGenericDefinition, GSVMLicense>> resultMap = new HashMap<>();
//        Map<GSVMGenericDefinition, Integer> map = new HashMap<>();
//        for (GSVMBidder bidder : bidders) {
//            GSVM_DemandQueryMIP mip = new GSVM_DemandQueryMIP(bidder, prices);
//            GSVMDemandQueryMipResult result = mip.getResult();
//            for (Map.Entry<GSVMGenericDefinition, Integer> entry : result.getResultingBundle().getQuantities().entrySet()) {
//                if (!map.containsKey(entry.getKey())) {
//                    map.put(entry.getKey(), 0);
//                }
//                map.merge(entry.getKey(), entry.getValue(), Integer::sum);
//            }
//            logger.info(result.getResultingBundle());
//            resultMap.put(bidder, result.getResultingBundle());
//            // Make sure a local bidder only picks his region
//            Assert.assertEquals(1, result.getResultingBundle().getSize());
//        }
//
//        for (int picks : map.values()) {
//            // Make sure every region is only picked once
//            Assert.assertEquals(1, picks);
//        }
//
//    }
//
//    @Test
//    public void minimalWorldNationalBidderDemandQueryTest() {
//
//        Set<GSVMNationalBidderSetup> nationalBidderSetups = new HashSet<>();
//
//        GSVMNationalBidderSetup.Builder nationalBuilder = new GSVMNationalBidderSetup.Builder();
//        nationalBuilder.setNumberOfBidders(1);
//        nationalBidderSetups.add(nationalBuilder.build());
//
//
//        GSVMWorld world = new MultiRegionModel().createWorld(new JavaUtilRNGSupplier(74563245));
//        GSVMBidder bidder = world.createPopulation(new HashSet<>(), new HashSet<>(), nationalBidderSetups,  new JavaUtilRNGSupplier(654798)).iterator().next();
//        Map<GSVMGenericDefinition, BigDecimal> prices = new HashMap<>();
//        world.getAllGenericDefinitions().forEach(def -> prices.put((GSVMGenericDefinition) def, BigDecimal.ZERO));
//        GSVMRegionsMap.Region region = world.getRegionsMap().getRegions().stream().findAny().get();
//        Set<GSVMGenericDefinition> genericDefinitions = prices.keySet().stream().filter(def -> def.getRegion().equals(region)).collect(Collectors.toSet());
//
//        // Assert that the bidder doesn't choose licenses from a region because it's too expensive
//        genericDefinitions.forEach(def -> prices.put(def, BigDecimal.valueOf(1000000000)));
//        GSVM_DemandQueryMIP mip = new GSVM_DemandQueryMIP(bidder, prices);
//        GSVMDemandQueryMipResult result = mip.getResult();
//        Set<GSVMRegionsMap.Region> regionsCovered = new HashSet<>();
//        result.getResultingBundle().getQuantities().entrySet().stream().filter(e -> e.getValue() > 0)
//                .forEach(e -> regionsCovered.add(e.getKey().getRegion()));
//        Assert.assertEquals(regionsCovered.size(), world.getRegionsMap().getNumberOfRegions() - 1);
//
//        // Assert that the bidder still chooses the licenses because the prices are less than the discount for losing a region
//        genericDefinitions.forEach(l -> prices.put(l, BigDecimal.valueOf(1000)));
//        mip = new GSVM_DemandQueryMIP(bidder, prices);
//        result = mip.getResult();
//        Set<GSVMRegionsMap.Region> regionsCovered2 = new HashSet<>();
//        result.getResultingBundle().getQuantities().entrySet().stream().filter(e -> e.getValue() > 0)
//                .forEach(e -> regionsCovered2.add(e.getKey().getRegion()));
//        Assert.assertEquals(regionsCovered2.size(), world.getRegionsMap().getNumberOfRegions());
//
//    }
}
