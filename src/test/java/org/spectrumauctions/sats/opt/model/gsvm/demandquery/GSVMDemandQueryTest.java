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

    @Test
    public void testAllBiddersInGSVM() {
        List<GSVMBidder> bidders = new GlobalSynergyValueModel().createPopulation(73246104);
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
}
