package org.spectrumauctions.sats.opt.model.lsvm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.price.LinearPrices;
import org.marketdesignresearch.mechlib.core.price.Price;
import org.marketdesignresearch.mechlib.core.price.Prices;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorld;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        Map<Good, Price> priceMap = new HashMap<>();
        world.getLicenses().forEach(license -> priceMap.put(license, Price.of(10)));
        Prices prices = new LinearPrices(priceMap);

        for (LSVMBidder bidder : bidders) {
            List<Bundle> results = bidder.getBestBundles(prices, 10);
            Assert.assertEquals(10, results.size());
            results.forEach(bundle -> {
                BigDecimal value = bidder.calculateValue(bundle);
                Price price = prices.getPrice(bundle);
                BigDecimal utility = bidder.getUtility(bundle, prices);
                Assert.assertTrue(utility.compareTo(BigDecimal.ZERO) > 0);
                logger.info("Bidder {} chooses bundle [{}].\tValue: {}\tPrice: {}\tUtility: {})",
                        bidder.getName(),
                        bundle,
                        value.setScale(2, RoundingMode.HALF_UP),
                        price.getAmount().setScale(2, RoundingMode.HALF_UP),
                        utility.setScale(2, RoundingMode.HALF_UP));
            });
        }
    }

}
