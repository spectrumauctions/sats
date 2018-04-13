package org.spectrumauctions.sats.opt.model.mrvm.demandquery;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
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
        Map<MRVMLicense, BigDecimal> prices = new HashMap<>();
        world.getLicenses().forEach(l -> prices.put(l, BigDecimal.valueOf(1000000)));

        for (MRVMBidder bidder : bidders) {
            MRVM_DemandQueryMIP mip = new MRVM_DemandQueryMIP(bidder, prices);
            MRVMDemandQueryMipResult result = mip.getResult();
            Assert.assertTrue(result.getResultingBundle().getValue().doubleValue() > 0);
            logger.info(result.getResultingBundle());
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

        Map<MRVMLicense, BigDecimal> prices = new HashMap<>();
        minimalWorld.getLicenses().forEach(l -> prices.put(l, BigDecimal.valueOf(10000)));

        Map<MRVMBidder, GenericValue<MRVMGenericDefinition, MRVMLicense>> resultMap = new HashMap<>();
        Map<MRVMGenericDefinition, Integer> map = new HashMap<>();
        for (MRVMBidder bidder : bidders) {
            MRVM_DemandQueryMIP mip = new MRVM_DemandQueryMIP(bidder, prices);
            MRVMDemandQueryMipResult result = mip.getResult();
            for (Map.Entry<MRVMGenericDefinition, Integer> entry : result.getResultingBundle().getQuantities().entrySet()) {
                if (!map.containsKey(entry.getKey())) {
                    map.put(entry.getKey(), 0);
                }
                map.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
            logger.info(result.getResultingBundle());
            resultMap.put(bidder, result.getResultingBundle());
            // Make sure a local bidder only picks his region
            Assert.assertEquals(1, result.getResultingBundle().getSize());
        }

        for (int picks : map.values()) {
            // Make sure every region is only picked once
            Assert.assertEquals(1, picks);
        }

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

        Map<MRVMLicense, BigDecimal> prices = new HashMap<>();
        minimalWorld.getLicenses().forEach(l -> prices.put(l, BigDecimal.valueOf(10000)));

        Map<MRVMBidder, GenericValue<MRVMGenericDefinition, MRVMLicense>> resultMap = new HashMap<>();
        Map<MRVMGenericDefinition, Integer> map = new HashMap<>();
        for (MRVMBidder bidder : bidders) {
            MRVM_DemandQueryMIP mip = new MRVM_DemandQueryMIP(bidder, prices);
            MRVMDemandQueryMipResult result = mip.getResult();
            for (Map.Entry<MRVMGenericDefinition, Integer> entry : result.getResultingBundle().getQuantities().entrySet()) {
                if (!map.containsKey(entry.getKey())) {
                    map.put(entry.getKey(), 0);
                }
                map.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
            logger.info(result.getResultingBundle());
            resultMap.put(bidder, result.getResultingBundle());
            // Make sure a local bidder only picks his region
            Assert.assertEquals(1, result.getResultingBundle().getSize());
        }

        for (int picks : map.values()) {
            // Make sure every region is only picked once
            Assert.assertEquals(1, picks);
        }

    }

    @Test
    public void minimalWorldNationalBidderDemandQueryTest() {

        Set<MRVMNationalBidderSetup> nationalBidderSetups = new HashSet<>();

        MRVMNationalBidderSetup.Builder nationalBuilder = new MRVMNationalBidderSetup.Builder();
        nationalBuilder.setNumberOfBidders(1);
        nationalBidderSetups.add(nationalBuilder.build());


        MRVMWorld world = new MultiRegionModel().createWorld(new JavaUtilRNGSupplier(74563245));
        MRVMBidder bidder = world.createPopulation(new HashSet<>(), new HashSet<>(), nationalBidderSetups,  new JavaUtilRNGSupplier(654798)).iterator().next();
        Map<MRVMLicense, BigDecimal> prices = new HashMap<>();
        world.getLicenses().forEach(l -> prices.put(l, BigDecimal.ZERO));
        MRVMRegionsMap.Region region = world.getRegionsMap().getRegions().stream().findAny().get();
        Set<MRVMLicense> licenses = world.getLicenses().stream().filter(l -> l.getRegion().equals(region)).collect(Collectors.toSet());

        // Assert that the bidder doesn't choose licenses from a region because it's too expensive
        licenses.forEach(l -> prices.put(l, BigDecimal.valueOf(1000000000)));
        MRVM_DemandQueryMIP mip = new MRVM_DemandQueryMIP(bidder, prices);
        MRVMDemandQueryMipResult result = mip.getResult();
        Set<MRVMRegionsMap.Region> regionsCovered = new HashSet<>();
        result.getResultingBundle().getQuantities().entrySet().stream().filter(e -> e.getValue() > 0)
                .forEach(e -> regionsCovered.add(e.getKey().getRegion()));
        Assert.assertEquals(regionsCovered.size(), world.getRegionsMap().getNumberOfRegions() - 1);

        // Assert that the bidder still chooses the licenses because the prices are less than the discount for losing a region
        licenses.forEach(l -> prices.put(l, BigDecimal.valueOf(1000)));
        mip = new MRVM_DemandQueryMIP(bidder, prices);
        result = mip.getResult();
        Set<MRVMRegionsMap.Region> regionsCovered2 = new HashSet<>();
        result.getResultingBundle().getQuantities().entrySet().stream().filter(e -> e.getValue() > 0)
                .forEach(e -> regionsCovered2.add(e.getKey().getRegion()));
        Assert.assertEquals(regionsCovered2.size(), world.getRegionsMap().getNumberOfRegions());

    }
}
