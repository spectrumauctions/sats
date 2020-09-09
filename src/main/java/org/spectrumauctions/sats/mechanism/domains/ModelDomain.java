package org.spectrumauctions.sats.mechanism.domains;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.BundleEntry;
import org.marketdesignresearch.mechlib.core.Domain;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.bidder.valuefunction.BundleValue;
import org.marketdesignresearch.mechlib.core.price.LinearPrices;
import org.marketdesignresearch.mechlib.core.price.Price;
import org.marketdesignresearch.mechlib.core.price.Prices;
import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.opt.model.ModelMIP;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode
@Slf4j
public abstract class ModelDomain<T extends SATSBidder> implements Domain {

    @Getter
    private List<T> bidders;

    private transient Allocation efficientAllocation;

    public abstract ModelMIP getMIP();
    
    private boolean generic;
    
    @Setter @Getter(value = AccessLevel.PROTECTED)
    private int priceGenerationBidsPerBidder = 100;
    @Setter
    private int priceGenerationNumberOfWorldSamples = 100;
    @Setter
    private double priceGenerationFraction = 0.01;
    @Setter
    private long priceGenerationSeed = System.currentTimeMillis();

    public ModelDomain(List<T> bidders) {
        this.bidders = bidders;
    }

    @Override
    public Allocation getEfficientAllocation() {
        if (!hasEfficientAllocationCalculated()) {
            getMIP().setMipInstrumentation(getMipInstrumentation());
            getMIP().setPurpose(MipInstrumentation.MipPurpose.ALLOCATION.name());
            efficientAllocation = getMIP().getAllocation();
        }
        return efficientAllocation;
    }

    @Override
    public boolean hasEfficientAllocationCalculated() {
        return efficientAllocation != null;
    }

    @Override
    public String getName() {
        return "SATS Domain";
    }

    // region instrumentation
    @Getter
    private MipInstrumentation mipInstrumentation = MipInstrumentation.NO_OP;

    @Override
    public void setMipInstrumentation(MipInstrumentation mipInstrumentation) {
        this.mipInstrumentation = mipInstrumentation;
        getBidders().forEach(bidder -> bidder.setMipInstrumentation(mipInstrumentation));
    }
    // endregion

    @Override
	public Prices proposeStartingPrices() {
    	try {
            Map<Good, SimpleRegression> regressions = new HashMap<>();
            for (Good genericDefinition : this.getGoods()) {
                SimpleRegression regression = new SimpleRegression(false);
                regression.addData(0.0, 0.0);
                regressions.put(genericDefinition, regression);
            }

            RNGSupplier rngSupplier = new JavaUtilRNGSupplier(priceGenerationSeed);
            for (int i = 0; i < priceGenerationNumberOfWorldSamples; i++) {
            	
                List<SATSBidder> alternateBidders = bidders.stream().map(b -> b.drawSimilarBidder(rngSupplier)).collect(Collectors.toList());
                for (SATSBidder bidder : alternateBidders) {
                	Iterator<BundleValue> bidIterator = createPriceSamplingBiddingLanguage(rngSupplier, bidder, this.getPriceGenerationBidsPerBidder()).iterator();
                    while (bidIterator.hasNext()) {
                        BundleValue bid = bidIterator.next();
                        for(BundleEntry entry : bid.getBundle().getBundleEntries()) {
                        	double y = bid.getAmount().doubleValue() * entry.getAmount() / bid.getBundle().getTotalAmount();
                        	regressions.get(entry.getGood()).addData((double)entry.getAmount(),y);
                        }
                    }
                }
            }
           
            
            double min = Double.MAX_VALUE;
            Map<Good, Price> priceMap = new HashMap<>();
            
            for (Map.Entry<Good, SimpleRegression> entry : regressions.entrySet()) {
                double y = entry.getValue().predict(1);
                double price = y * priceGenerationFraction;
                log.info("{}:\nFound prediction of {}, setting starting price to {}.",
                        entry.getKey(), y, price);
                priceMap.put(entry.getKey(), new Price(BigDecimal.valueOf(price)));
                if (price > 0 && price < min) min = price;
            }

            // If ever a prediction turns out to be zero or negative (which should almost never be the case)
            // it is set to the smallest prediction of the other goods to avoid getting stuck in CCA
            for (Good def: regressions.keySet()) {
                if (priceMap.get(def).getAmount().compareTo(BigDecimal.ZERO) < 1) {
                    priceMap.put(def, new Price(BigDecimal.valueOf(min)));
                }
            }
            return new LinearPrices(priceMap);

        } catch (UnsupportedBiddingLanguageException e) {
            // Catching this error here, because it's very unlikely to happen and we don't want to bother
            // the user with handling this error. We just log it and don't set the starting prices.
        	// TODO
            log.error("Tried to calculate sampled starting prices, but {} doesn't support the " +
                    "SizeBasedUniqueRandomXOR bidding language. Not setting any starting prices.", this);
        }
        // TODO Exception handling
        return null;
    }

	public abstract BiddingLanguage createPriceSamplingBiddingLanguage(RNGSupplier rngSupplier, SATSBidder bidder, int numberOfSamples)
			throws UnsupportedBiddingLanguageException;

}
