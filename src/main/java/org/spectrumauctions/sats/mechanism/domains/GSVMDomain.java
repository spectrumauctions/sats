package org.spectrumauctions.sats.mechanism.domains;

import java.util.List;

import org.marketdesignresearch.mechlib.core.Good;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
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
	protected ModelMIP getMIP() {
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
		SizeBasedUniqueRandomXOR valueFunction;
		valueFunction = bidder.getValueFunction(SizeBasedUniqueRandomXOR.class, rngSupplier);
		valueFunction.setIterations(numberOfSamples);
		return valueFunction;
	}
}
