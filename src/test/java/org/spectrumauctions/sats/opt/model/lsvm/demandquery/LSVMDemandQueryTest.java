package org.spectrumauctions.sats.opt.model.lsvm.demandquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorld;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
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
        world.getLicenses().forEach(license -> prices.put(license, BigDecimal.valueOf(0)));

        for (LSVMBidder bidder : bidders) {
            LSVM_DemandQueryMIP mip = new LSVM_DemandQueryMIP(bidder, prices);
            LSVM_DemandQueryMipResult result = mip.getResult();
            Assert.assertTrue(result.getResultingBundle().value().doubleValue() > 0);
            logger.info(result.getResultingBundle());
        }
    }
}
