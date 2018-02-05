package org.spectrumauctions.sats.core.bidlang.xor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.cats.CATSBidder;
import org.spectrumauctions.sats.core.model.cats.CATSLicense;
import org.spectrumauctions.sats.core.model.cats.CATSRegionModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CatsXORTest {

    private static final Logger logger = LogManager.getLogger(CatsXORTest.class);

    @Test
    public void testSimple() {
        CatsXOR valueFunction;
        CATSBidder bidder = new CATSRegionModel().createNewPopulation().stream().findAny().orElseThrow(IllegalArgumentException::new);
        try {
            valueFunction = bidder.getValueFunction(CatsXOR.class);
            Iterator<XORValue<CATSLicense>> catsIterator = valueFunction.iterator();
            Set<XORValue<CATSLicense>> bids = new HashSet<>();

            while (catsIterator.hasNext()) {
                bids.add(catsIterator.next());
            }

            Assert.assertNotNull(bids);

        } catch (UnsupportedBiddingLanguageException e) {
            logger.error("Unsupported bidding language!");
            Assert.fail();
        }
    }
}
