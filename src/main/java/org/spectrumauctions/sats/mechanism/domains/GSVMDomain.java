package org.spectrumauctions.sats.mechanism.domains;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.bidder.valuefunction.BundleValue;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;

public class GSVMDomain extends ModelDomain<GSVMBidder> {

	public GSVMDomain(List<GSVMBidder> bidders) {
		super(bidders);
	}

	@Override
	public ModelMIP getMIP() {
		return new GSVMStandardMIP(getBidders().get(0).getWorld(), getBidders());
	}

	@Override
	public String getName() {
		return super.getName() + " (GSVM)";
	}

	@Override
	public List<? extends Good> getGoods() {
		return getBidders().iterator().next().getWorld().getLicenses();
	}

	@Override
	public BiddingLanguage createPriceSamplingBiddingLanguage(RNGSupplier rngSupplier, SATSBidder bidder, int numberOfSamples)
			throws UnsupportedBiddingLanguageException {
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
}
