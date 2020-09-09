package org.spectrumauctions.sats.mechanism.domains;

import java.util.List;

import org.marketdesignresearch.mechlib.core.Good;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;

public class LSVMDomain extends ModelDomain<LSVMBidder> {

	public LSVMDomain(List<LSVMBidder> bidders) {
		super(bidders);
	}

	@Override
	public ModelMIP getMIP() {
		return new LSVMStandardMIP(getBidders());
	}

	@Override
	public String getName() {
		return super.getName() + " (LSVM)";
	}

	@Override
	public List<? extends Good> getGoods() {
		return getBidders().iterator().next().getWorld().getLicenses();
	}

	@Override
	public BiddingLanguage createPriceSamplingBiddingLanguage(RNGSupplier rngSupplier, SATSBidder bidder, int numberOfSamples)
			throws UnsupportedBiddingLanguageException {
		
		SizeBasedUniqueRandomXOR valueFunction;
		valueFunction = bidder.getValueFunction(SizeBasedUniqueRandomXOR.class, rngSupplier);
		valueFunction.setIterations(numberOfSamples);
		return valueFunction;
	}

}
