package org.spectrumauctions.sats.opt.model.mrvm.demandquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public class MRVMDemandQueryTest {

    private static final Logger logger = LogManager.getLogger(MRVMDemandQueryTest.class);

    @Test
    public void demandQueryTest() {
        List<MRVMBidder> bidders = new MultiRegionModel().createNewPopulation();
        MRVMBidder bidder = bidders.iterator().next();
        Map<MRVMLicense, BigDecimal> prices = new HashMap<>();
        bidder.getWorld().getLicenses().forEach(l -> prices.put(l, BigDecimal.ZERO));

        MRVM_DemandQueryMIP mip = new MRVM_DemandQueryMIP(bidder, prices);
        MRVMDemandQueryMipResult result = mip.calculateAllocation();
        logger.info(result);
    }
}
