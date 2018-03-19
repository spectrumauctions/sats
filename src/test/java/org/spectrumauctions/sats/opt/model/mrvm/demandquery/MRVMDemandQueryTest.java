package org.spectrumauctions.sats.opt.model.mrvm.demandquery;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.util.math.LinearFunction;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @author Fabio Isler
 */
public class MRVMDemandQueryTest {

    private static final Logger logger = LogManager.getLogger(MRVMDemandQueryTest.class);

    @Test
    public void standardDemandQueryTest() {
        List<MRVMBidder> bidders = new MultiRegionModel().createNewPopulation();
        MRVMBidder bidder = bidders.iterator().next();
        Map<MRVMLicense, BigDecimal> prices = new HashMap<>();
        bidder.getWorld().getLicenses().forEach(l -> prices.put(l, BigDecimal.valueOf(10000000 + Math.random() * 5000000)));

        MRVM_DemandQueryMIP mip = new MRVM_DemandQueryMIP(bidder, prices);
        MRVMDemandQueryMipResult result = mip.calculateAllocation();
        logger.info(result.toString());
        logger.info(result.getResultingBundle());
    }

    @Test
    public void minimalWorldLocalBidderDemandQueryTest() {
        MRVMWorldSetup.MRVMWorldSetupBuilder worldSetupBuilder = new MRVMWorldSetup.MRVMWorldSetupBuilder();
        worldSetupBuilder.createGraphRandomly(new IntegerInterval(10), new IntegerInterval(0), 100000, 0);
        Set<String> oldBands = new HashSet<>(worldSetupBuilder.bandSetups().keySet());

        oldBands.forEach(worldSetupBuilder::removeBandSetup);

        MRVMWorldSetup.BandSetup bandSetup = new MRVMWorldSetup.BandSetup(
                "A",
                new IntegerInterval(1),
                new DoubleInterval(20),
                new LinearFunction(BigDecimal.ONE, BigDecimal.ZERO));
        worldSetupBuilder.putBandSetup(bandSetup);

        MRVMWorld world = new MRVMWorld(worldSetupBuilder.build(), new JavaUtilRNGSupplier(103103));
        Set<MRVMRegionsMap.Region> regions = world.getRegionsMap().getRegions();
        Set<MRVMLocalBidderSetup> localBidderSetups = new HashSet<>();
        for (MRVMRegionsMap.Region region : regions) {
            MRVMLocalBidderSetup.Builder localBuilder = new MRVMLocalBidderSetup.Builder();
            localBuilder.setNumberOfBidders(1);
            localBuilder.setPredefinedRegionsOfInterest(Lists.newArrayList(region.getNote()));
            localBidderSetups.add(localBuilder.build());
        }

        List<MRVMBidder> bidders = world.createPopulation(localBidderSetups, new HashSet<>(), new HashSet<>(), new JavaUtilRNGSupplier(654798));

        Map<MRVMLicense, BigDecimal> prices = new HashMap<>();
        world.getLicenses().forEach(l -> prices.put(l, BigDecimal.valueOf(10000)));

        Map<MRVMBidder, GenericValue<MRVMGenericDefinition>> resultMap = new HashMap<>();
        Map<MRVMGenericDefinition, Integer> map = new HashMap<>();
        for (MRVMBidder bidder : bidders) {
            MRVM_DemandQueryMIP mip = new MRVM_DemandQueryMIP(bidder, prices);
            MRVMDemandQueryMipResult result = mip.calculateAllocation();
            for (Map.Entry<MRVMGenericDefinition, Integer> entry : result.getResultingBundle().getQuantities().entrySet()) {
                if (!map.containsKey(entry.getKey())) {
                    map.put(entry.getKey(), 0);
                }
                map.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
            logger.info(result.getResultingBundle());
            resultMap.put(bidder, result.getResultingBundle());
            Assert.assertEquals(1, result.getResultingBundle().getSize());
        }

        for (int picks : map.values()) {
            Assert.assertEquals(1, picks);
        }

    }
}
