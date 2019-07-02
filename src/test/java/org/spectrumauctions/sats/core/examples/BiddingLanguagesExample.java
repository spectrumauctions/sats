package org.spectrumauctions.sats.core.examples;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import org.spectrumauctions.sats.core.bidlang.xor.*;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SingleRegionModel;

import java.util.Iterator;

/**
 * <p>These examples show how to use bidding languages.
 * <p>They require a bidder instance - to see how those can be generated consider the examples of {@link SimpleModelAccessorsExample}
 * and {@link ParameterizingModelsExample}
 */
public class BiddingLanguagesExample {

    private static final Logger logger = LogManager.getLogger(BiddingLanguagesExample.class);

    /**
     * See {@link SimpleModelAccessorsExample} and {@link ParameterizingModelsExample} for examples how bidders can be generated
     */
    private static SRVMBidder createAnyBidder() {
        return new SingleRegionModel().createNewPopulation().stream().findAny().orElse(null);
    }

    /**
     * This example shows how to request a specific bidding language. The language used for this example is {@link SizeBasedUniqueRandomXOR}, but there are several others available.
     * XOR-Based languages, amongst them:
     * <p>XOR-Based
     * <ul>
     * <li>{@link SizeBasedUniqueRandomXOR}- Randomly distributed XOR Bids</li>
     * <li>{@link IncreasingSizeOrderedXOR} and {@link DecreasingSizeOrderedXOR} - XOR Bundles deterministically returned and ordered by size</li>
     * <li>{@link BidderSpecificXOR} - Items are ordered in a way specific to this bidder type. Might be randomized. </li>
     * </ul></p>
     * <p>XOR-Q-Based
     * <li>{@link GenericPowersetDecreasing} and {@link GenericPowersetIncreasing} - XOR-Q Bundles deterministically returned and ordered by size</li>
     * //TODO Complete list
     * </p>
     * {@link }
     */
    @Test
    public void generateRandomOrderXORBids() {
        SATSBidder bidder = createAnyBidder();
        SizeBasedUniqueRandomXOR<?> valueFunction;
        try {
            // Get a SizeBaseUniqueRandom XOR Iterator from your bidder
            valueFunction = (SizeBasedUniqueRandomXOR) bidder.getValueFunction(SizeBasedUniqueRandomXOR.class);
            //If you want, you can override defaults
            int meanBundleSize = 3;
            double standardDeviation = 0;
            valueFunction.setDistribution(meanBundleSize, standardDeviation);
            int numberOfBids = 30; // More bids than specified here must not be requested.
            valueFunction.setIterations(numberOfBids);
            // Do something with the generated bids
            Iterator<? extends XORValue<?>> xorBidIterator = valueFunction.iterator();
            while (xorBidIterator.hasNext()) {
                XORValue bid = xorBidIterator.next();
                logger.info(bid.getLicenses().toString() + "   " + bid.value().toString());
            }
        } catch (UnsupportedBiddingLanguageException e) {
            // If the model does not support the specified value function, this exception is thrown.
        }
    }
}
