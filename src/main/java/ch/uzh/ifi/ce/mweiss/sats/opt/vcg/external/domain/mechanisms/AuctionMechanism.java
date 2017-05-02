package ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.mechanisms;

import ch.uzh.ifi.ce.mweiss.sats.opt.model.EfficientAllocator;
import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.AuctionResult;

public interface AuctionMechanism extends PaymentMechanism, EfficientAllocator {
	AuctionResult getAuctionResult();
}
