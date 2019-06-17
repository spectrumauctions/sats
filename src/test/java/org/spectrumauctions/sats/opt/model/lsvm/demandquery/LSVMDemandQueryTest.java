package org.spectrumauctions.sats.opt.model.lsvm.demandquery;

import com.google.common.collect.Lists;
import edu.harvard.econcs.jopt.solver.SolveParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.lsvm.*;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public class LSVMDemandQueryTest {

    private static final Logger logger = LogManager.getLogger(LSVMDemandQueryTest.class);

    @Test
    public void testAllBiddersInLSVM() {
        List<LSVMBidder> bidders = new LocalSynergyValueModel().createNewPopulation(new JavaUtilRNGSupplier(73246104));
        LSVMWorld world = bidders.iterator().next().getWorld();
        Map<LSVMLicense, BigDecimal> prices = new HashMap<>();
        world.getLicenses().forEach(license -> prices.put(license, BigDecimal.TEN));

        for (LSVMBidder bidder : bidders) {
            LSVM_DemandQueryMIP mip = new LSVM_DemandQueryMIP(bidder, prices);
            List<LSVM_DemandQueryMipResult> results = mip.getResultPool(10);
            Assert.assertEquals(10, results.size());
            results.forEach(result -> Assert.assertTrue(result.getResultingBundle().value().doubleValue() > 0));
            logger.info(results);
        }
    }

    @Test
    public void testNationalBidder() {
        JavaUtilRNGSupplier rng = new JavaUtilRNGSupplier(73246104);
        LSVMWorldSetup.LSVMWorldSetupBuilder worldSetupBuilder = new LSVMWorldSetup.LSVMWorldSetupBuilder();
        worldSetupBuilder.setNumberOfColumnsInterval(new IntegerInterval(1));
        worldSetupBuilder.setNumberOfRowsInterval(new IntegerInterval(6));

        LSVMWorld world = new LSVMWorld(worldSetupBuilder.build(), rng);

        LSVMBidderSetup.NationalBidderBuilder nationalBidderBuilder = new LSVMBidderSetup.NationalBidderBuilder();
        nationalBidderBuilder.setValueInterval(new DoubleInterval(4));

        List<LSVMBidder> bidders = world.createPopulation(Lists.newArrayList(nationalBidderBuilder.build()), rng);
        Map<LSVMLicense, BigDecimal> prices = new HashMap<>();
        world.getLicenses().forEach(license -> prices.put(license, BigDecimal.valueOf(3)));

        Assert.assertEquals(1, bidders.size());

        LSVMBidder bidder = bidders.get(0);

        LSVM_DemandQueryMIP mip = new LSVM_DemandQueryMIP(bidder, prices);

        logger.info("#Vars: \t{}", mip.getMip().getMip().getNumVars());
        logger.info("#Constraints: \t{}", mip.getMip().getMip().getNumConstraints());
        List<LSVM_DemandQueryMipResult> result = mip.getResultPool(10);
        Assert.assertEquals(10, result.size());
    }
}
