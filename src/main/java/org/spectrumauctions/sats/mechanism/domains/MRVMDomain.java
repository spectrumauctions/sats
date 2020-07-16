package org.spectrumauctions.sats.mechanism.domains;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.bidder.valuefunction.BundleValue;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.mrvm.MRVMAllocationLimit;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMGenericAllocationLimit;
import org.spectrumauctions.sats.core.model.mrvm.MRVMNationalBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMNationalBidderAllocationLimit;
import org.spectrumauctions.sats.core.model.mrvm.MRVMNationalBidderGenericAllocationLimit;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

public class MRVMDomain extends ModelDomain<MRVMBidder> {

	private boolean generic;
	private boolean allocationLimits = false;
	
    public MRVMDomain(List<MRVMBidder> bidders) {
        this(bidders, true);
    }
    
    public MRVMDomain(List<MRVMBidder> bidders, boolean generic) {
        this(bidders,generic,false);
    }
    
    public MRVMDomain(List<MRVMBidder> bidders, boolean generic, boolean addAllocationLimits) {
        super(bidders);
        this.generic = generic;
        this.allocationLimits = addAllocationLimits;
        
        if(addAllocationLimits) {
        	for(MRVMBidder bidder : bidders) {
        		if(bidder instanceof MRVMNationalBidder) {
        			bidder.setAllocationLimit(generic ? new MRVMNationalBidderGenericAllocationLimit((MRVMNationalBidder)bidder) : new MRVMNationalBidderAllocationLimit((MRVMNationalBidder)bidder));
        		} else {
        			bidder.setAllocationLimit(generic ? new MRVMGenericAllocationLimit(bidder) : new MRVMAllocationLimit(bidder));
        		}
        	}
        }
    }

    @Override
    protected ModelMIP getMIP() {
        return new MRVM_MIP(getBidders());
    }

    @Override
    public String getName() {
        return super.getName() + " (MRVM)";
    }
	@Override
	public List<? extends Good> getGoods() {
		if (generic) {
            return getBidders().iterator().next().getWorld().getAllGenericDefinitions();
        } else {
            return getBidders().iterator().next().getWorld().getLicenses();
        }
	}
	
	@Override
	public BiddingLanguage createPriceSamplingBiddingLanguage(RNGSupplier rngSupplier, SATSBidder bidder, int numberOfSamples)
			throws UnsupportedBiddingLanguageException {
		if(allocationLimits) {
			return new BiddingLanguage() {
				
				@Override
				public SATSBidder getBidder() {
					return bidder;
				}

				@Override
				public Iterator<BundleValue> iterator() {
					return new Iterator<BundleValue>() {
						
						private int number=0;
						private Random random = new Random(rngSupplier.getUniformDistributionRNG().nextLong());
						
						@Override
						public boolean hasNext() {
							return number < numberOfSamples;
						}

						@Override
						public BundleValue next() {
							number++;
							Bundle bundle = bidder.getAllocationLimit().getUniformRandomBundle(random, getGoods());
							return new BundleValue(bidder.calculateValue(bundle), bundle);
						}
					};
				};
			};
		}
		
		if(generic) {
			XORQRandomOrderSimple valueFunction;
			valueFunction = bidder.getValueFunction(XORQRandomOrderSimple.class, rngSupplier);
			valueFunction.setIterations(numberOfSamples);
			return valueFunction;
		} else {
			SizeBasedUniqueRandomXOR valueFunction;
			valueFunction = bidder.getValueFunction(SizeBasedUniqueRandomXOR.class, rngSupplier);
			valueFunction.setIterations(numberOfSamples);
			return valueFunction;
		}
	}

}
