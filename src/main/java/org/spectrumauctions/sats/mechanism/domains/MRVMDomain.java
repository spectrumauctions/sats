package org.spectrumauctions.sats.mechanism.domains;

import java.util.List;

import org.marketdesignresearch.mechlib.core.Good;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

public class MRVMDomain extends ModelDomain<MRVMBidder> {

	private boolean generic;
	
    public MRVMDomain(List<MRVMBidder> bidders) {
        this(bidders, true);
    }
    public MRVMDomain(List<MRVMBidder> bidders, boolean generic) {
        super(bidders);
        this.generic = generic;
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
