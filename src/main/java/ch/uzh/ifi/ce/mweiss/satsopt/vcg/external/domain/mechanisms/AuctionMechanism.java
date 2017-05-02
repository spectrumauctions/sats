package ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.mechanisms;

import ch.uzh.ifi.ce.mweiss.satsopt.model.EfficientAllocator;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.AuctionResult;

public interface AuctionMechanism extends PaymentMechanism, EfficientAllocator {
	AuctionResult getAuctionResult();
}
