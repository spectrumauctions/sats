package ch.uzh.ifi.ce.mweiss.sats.core.examples;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.*;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.srm.SRMBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.srm.SingleRegionModel;
import org.junit.Test;

import java.util.Iterator;

/**
 * <p>These examples show how to use bidding languages.
 * <p>They require a bidder instance - to see how those can be generated consider the examples of {@link SimpleModelAccessorsExample}
 * and {@link ParameterizingModelsExample}
 */
public class BiddingLanguagesExample {

    /**
     * See {@link SimpleModelAccessorsExample} and {@link ParameterizingModelsExample} for examples how bidders can be generated
     */
    private static SRMBidder createAnyBidder(){
        return new SingleRegionModel().createNewPopulation().stream().findAny().orElse(null);
    }

    /**
     * This example shows how to request a specific bidding language. The language used for this example is {@link SizeBasedUniqueRandomXOR}, but there are several others available.
     * XOR-Based languages, amongst them:
     * <p>XOR-Based
     * <ul>
     *     <li>{@link SizeBasedUniqueRandomXOR}- Randomly distributed XOR Bids</li>
     *     <li>{@link IncreasingSizeOrderedXOR} and {@link DecreasingSizeOrderedXOR} - XOR Bundles deterministically returned and ordered by size</li>
     *     <li>{@link BidderSpecificXOR} - Items are ordered in a way specific to this bidder type. Might be randomized. </li>
     * </ul></p>
     * <p>XOR-Q-Based
     *      <li>{@link GenericPowersetDecreasing} and {@link GenericPowersetIncreasing} - XOR-Q Bundles deterministically returned and ordered by size</li>
     *      //TODO Complete list
     * </p>
     *     {@link }
     */
    @Test
    public void generateRandomOrderXORBids(){
        Bidder bidder = createAnyBidder();
        SizeBasedUniqueRandomXOR<?> valueFunction;
        try {
            // Get a SizeBaseUniqueRandom XOR Iterator from your bidder
            valueFunction = (SizeBasedUniqueRandomXOR) bidder.getValueFunction(SizeBasedUniqueRandomXOR.class);
            //If you want, you can override defaults
            int meanBundleSize = 3;
            double standardDeviation = 0;
            int numberOfBids = 30; //More bids than specified here must not be requested.
            valueFunction.setDistribution(meanBundleSize, standardDeviation, numberOfBids);
            // Do something with the generated bids
            Iterator<? extends XORValue<?>> xorBidIterator = valueFunction.iterator();
            while (xorBidIterator.hasNext()){
                XORValue bid = xorBidIterator.next();
                System.out.println(bid.getLicenses().toString() + "   " + bid.value().toString());
            }
        } catch (UnsupportedBiddingLanguageException e) {
            // If the model does not support the specified value function, this exception is thrown.
        }
    }
}
